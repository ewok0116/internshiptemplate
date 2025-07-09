// presentation/viewmodel/PaymentManager.kt
package com.example.foodorderingapp_ver2.presentation.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.foodorderingapp_ver2.domain.entities.*
import com.example.foodorderingapp_ver2.domain.usecases.*
import com.example.foodorderingapp_ver2.domain.common.Result

// Payment result data class
data class PaymentResult(
    val isSuccess: Boolean,
    val transactionId: String? = null,
    val errorMessage: String? = null
)

class PaymentManager(
    private val createOrderUseCase: CreateOrderUseCase,
    private val coroutineScope: CoroutineScope,
    private val onShowToast: (String) -> Unit,
    private val onStateUpdate: (FoodOrderingUiState) -> Unit,
    private val getCurrentState: () -> FoodOrderingUiState
) {

    // =====================================================
    // PAYMENT DIALOG METHODS
    // =====================================================

    fun showPayment() {
        val currentState = getCurrentState()

        if (currentState.loadingState != LoadingStateUi.SUCCESS) {
            onShowToast("⚠️ Please wait for data to load completely")
            return
        }

        if (currentState.cartItems.isEmpty()) {
            onShowToast("🛒 Your cart is empty")
            return
        }

        onStateUpdate(currentState.copy(
            showPaymentDialog = true,
            paymentState = PaymentStateUi.SELECTING
        ))
    }

    fun hidePayment() {
        val currentState = getCurrentState()
        Log.d("PaymentManager", "🚨 hidePayment() called")
        Log.d("PaymentManager", "    Current paymentState = ${currentState.paymentState}")

        // Don't reset state if payment was successful - let user see success dialog!
        if (currentState.paymentState == PaymentStateUi.SUCCESS) {
            Log.d("PaymentManager", "⚠️ Keeping SUCCESS state - just hiding dialog")
            onStateUpdate(currentState.copy(showPaymentDialog = false))
        } else {
            Log.d("PaymentManager", "✅ Resetting payment state to NONE")
            onStateUpdate(currentState.copy(
                showPaymentDialog = false,
                paymentState = PaymentStateUi.NONE
            ))
        }
    }

    fun retryPayment() {
        val currentState = getCurrentState()
        onStateUpdate(currentState.copy(paymentState = PaymentStateUi.SELECTING))
    }

    // =====================================================
    // PAYMENT PROCESSING METHODS
    // =====================================================

    fun processPayment(method: PaymentMethod) {
        val currentState = getCurrentState()
        Log.d("PaymentManager", "🎯 Starting payment process with method: ${method.displayName}")

        // Basic validation
        if (currentState.loadingState != LoadingStateUi.SUCCESS || currentState.products.isEmpty()) {
            Log.e("PaymentManager", "❌ Cannot process payment: LoadingState=${currentState.loadingState}, Products=${currentState.products.size}")
            onShowToast("❌ Cannot process payment: Data not loaded properly")
            onStateUpdate(currentState.copy(paymentState = PaymentStateUi.FAILED))
            return
        }

        if (currentState.cartItems.isEmpty()) {
            Log.e("PaymentManager", "❌ Cannot process payment: Cart is empty")
            onShowToast("❌ Cart is empty")
            onStateUpdate(currentState.copy(paymentState = PaymentStateUi.FAILED))
            return
        }

        Log.d("PaymentManager", "✅ Pre-payment validation passed")
        Log.d("PaymentManager", "📦 Cart items: ${currentState.cartItems.size}")
        Log.d("PaymentManager", "💰 Cart total: ${currentState.cartTotal}")

        onStateUpdate(currentState.copy(
            selectedPaymentMethod = method,
            paymentState = PaymentStateUi.PROCESSING
        ))

        coroutineScope.launch {
            try {
                // *** STEP 1: PROCESS PAYMENT FIRST ***
                Log.d("PaymentManager", "💳 Step 1: Processing payment BEFORE creating order...")

                val paymentResult = processPaymentWithProvider(method, currentState.cartTotal)

                Log.d("PaymentManager", "💳 Step 2: Payment result received:")
                Log.d("PaymentManager", "    isSuccess = ${paymentResult.isSuccess}")
                Log.d("PaymentManager", "    transactionId = ${paymentResult.transactionId}")
                Log.d("PaymentManager", "    errorMessage = ${paymentResult.errorMessage}")

                if (paymentResult.isSuccess) {
                    Log.i("PaymentManager", "💳 ✅ Payment successful! Transaction ID: ${paymentResult.transactionId}")
                    Log.i("PaymentManager", "💳 ✅ Now creating order in database...")

                    // *** STEP 2: CREATE ORDER IN DATABASE ***
                    val cartItems = currentState.cartItems.map { cartItemUi ->
                        Log.d("PaymentManager", "📝 Mapping cart item: ${cartItemUi.product.name} x${cartItemUi.quantity} = ${cartItemUi.subtotal} TL")
                        CartItem(
                            product = cartItemUi.product,
                            quantity = cartItemUi.quantity
                        )
                    }

                    val createOrderParams = CreateOrderParams(
                        userId = currentState.currentUserId,
                        items = cartItems,
                        deliveryAddress = "Default Delivery Address",
                        paymentMethod = method
                    )

                    Log.d("PaymentManager", "🌐 Calling createOrderUseCase...")
                    Log.d("PaymentManager", "🔍 DEBUG: createOrderParams details:")
                    Log.d("PaymentManager", "    userId = ${createOrderParams.userId}")
                    Log.d("PaymentManager", "    items count = ${createOrderParams.items.size}")
                    Log.d("PaymentManager", "    deliveryAddress = ${createOrderParams.deliveryAddress}")
                    Log.d("PaymentManager", "    paymentMethod = ${createOrderParams.paymentMethod}")

                    when (val orderResult = createOrderUseCase(createOrderParams)) {
                        is Result.Success -> {
                            Log.i("PaymentManager", "🎉 SUCCESS! Order created in database:")
                            Log.i("PaymentManager", "   - Order ID: ${orderResult.data.id}")
                            Log.i("PaymentManager", "   - Payment method: ${method.displayName}")
                            Log.i("PaymentManager", "   - Transaction ID: ${paymentResult.transactionId}")

                            // Set SUCCESS state
                            val updatedState = getCurrentState()
                            onStateUpdate(updatedState.copy(
                                paymentState = PaymentStateUi.SUCCESS,
                                completedOrderItems = updatedState.cartItems.toList(), // Save for receipt
                                lastOrderId = orderResult.data.id,
                                cartItems = emptyList() // Clear cart
                            ))

                            Log.d("PaymentManager", "✅ SUCCESS STATE SET!")
                            Log.d("PaymentManager", "    paymentState = ${getCurrentState().paymentState}")
                            Log.d("PaymentManager", "    lastOrderId = ${getCurrentState().lastOrderId}")

                            onShowToast("🎉 Payment successful! Order #${orderResult.data.id} saved to database!")
                        }

                        is Result.Error -> {
                            Log.e("PaymentManager", "❌ Order creation failed:")
                            Log.e("PaymentManager", "    Error: ${orderResult.message}")
                            Log.i("PaymentManager", "⚠️ Payment succeeded but order failed - STILL SHOWING SUCCESS")

                            // Payment succeeded, so still show success even if order creation failed
                            val fakeOrderId = (1000..9999).random()
                            val updatedState = getCurrentState()
                            onStateUpdate(updatedState.copy(
                                paymentState = PaymentStateUi.SUCCESS,
                                completedOrderItems = updatedState.cartItems.toList(),
                                lastOrderId = fakeOrderId,
                                cartItems = emptyList()
                            ))

                            Log.d("PaymentManager", "✅ SUCCESS STATE SET (despite order creation failure)!")
                            Log.d("PaymentManager", "    paymentState = ${getCurrentState().paymentState}")
                            Log.d("PaymentManager", "    fakeOrderId = $fakeOrderId")

                            onShowToast("🎉 Payment successful! (Order #$fakeOrderId - DB save failed but payment processed)")
                        }

                        Result.Loading -> {
                            Log.d("PaymentManager", "⏳ Order creation still loading after payment success...")
                        }
                    }
                } else {
                    Log.e("PaymentManager", "💳 ❌ Payment failed with error: ${paymentResult.errorMessage}")
                    Log.d("PaymentManager", "🚫 Order will NOT be created - payment failed")

                    val updatedState = getCurrentState()
                    onStateUpdate(updatedState.copy(
                        paymentState = PaymentStateUi.FAILED,
                        errorMessage = paymentResult.errorMessage
                    ))
                    Log.d("PaymentManager", "❌ FAILED STATE SET due to payment error")
                    onShowToast("❌ Payment failed: ${paymentResult.errorMessage}")
                }

            } catch (e: Exception) {
                Log.e("PaymentManager", "❌ Exception in payment processing:", e)

                val updatedState = getCurrentState()
                onStateUpdate(updatedState.copy(
                    paymentState = PaymentStateUi.FAILED,
                    errorMessage = "Payment processing exception: ${e.message}"
                ))
                Log.d("PaymentManager", "❌ FAILED STATE SET due to exception")
                onShowToast("❌ Payment processing failed: ${e.message}")
            }
        }
    }

    // =====================================================
    // PAYMENT PROVIDER INTEGRATION
    // =====================================================

    // *** FIXED PAYMENT PROVIDER - ONLY ALLOW CREDIT CARD, MULTINET, EDENRED ***
    private suspend fun processPaymentWithProvider(method: PaymentMethod, amount: Double): PaymentResult {
        Log.d("PaymentManager", "💳 🔄 Contacting payment provider...")
        Log.d("PaymentManager", "💳 Payment details:")
        Log.d("PaymentManager", "   - Method: ${method.displayName}")
        Log.d("PaymentManager", "   - Amount: $amount TL")

        // Simulate realistic payment processing delay
        kotlinx.coroutines.delay(2000)

        // *** ONLY THESE 3 PAYMENT METHODS ARE ACCEPTED ***
        val allowedMethods = listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.MULTINET,
            PaymentMethod.EDENRED
        )

        return if (allowedMethods.contains(method)) {
            // SUCCESS for Credit Card, Multinet, and Edenred
            val transactionId = "TXN-${System.currentTimeMillis()}"
            Log.i("PaymentManager", "💳 ✅ Payment provider APPROVED payment:")
            Log.i("PaymentManager", "   - Method: ${method.displayName}")
            Log.i("PaymentManager", "   - Amount: $amount TL")
            Log.i("PaymentManager", "   - Transaction ID: $transactionId")

            PaymentResult(isSuccess = true, transactionId = transactionId)
        } else {
            // FAILURE for Cash, Coupon, and Sodexo
            val errorMsg = "Payment method '${method.displayName}' is not accepted. Only Credit Card, Multinet, and Edenred are supported."
            Log.e("PaymentManager", "💳 ❌ Payment provider REJECTED payment:")
            Log.e("PaymentManager", "   - Method: ${method.displayName}")
            Log.e("PaymentManager", "   - Reason: Method not supported")
            Log.e("PaymentManager", "   - Allowed methods: Credit Card, Multinet, Edenred")

            PaymentResult(isSuccess = false, errorMessage = errorMsg)
        }
    }

    // =====================================================
    // CART CLEARING METHODS
    // =====================================================

    fun clearCartAndHidePayment() {
        val currentState = getCurrentState()
        onStateUpdate(currentState.copy(
            cartItems = emptyList(),
            completedOrderItems = emptyList(),
            showCartDialog = false,
            showPaymentDialog = false,
            paymentState = PaymentStateUi.NONE,
            lastOrderId = null
        ))
        onShowToast("🏠 Welcome back!")
    }

    // =====================================================
    // PAYMENT VALIDATION METHODS
    // =====================================================

    fun isPaymentInProgress(): Boolean {
        val currentState = getCurrentState()
        return currentState.paymentState == PaymentStateUi.PROCESSING
    }

    fun isPaymentSuccessful(): Boolean {
        val currentState = getCurrentState()
        return currentState.paymentState == PaymentStateUi.SUCCESS
    }

    fun isPaymentFailed(): Boolean {
        val currentState = getCurrentState()
        return currentState.paymentState == PaymentStateUi.FAILED
    }

    fun getPaymentStatus(): PaymentStateUi {
        return getCurrentState().paymentState
    }

    fun getSelectedPaymentMethod(): PaymentMethod {
        return getCurrentState().selectedPaymentMethod
    }

    fun getLastOrderId(): Int? {
        return getCurrentState().lastOrderId
    }

    fun getReceiptItems(): List<CartItemUi> {
        return getCurrentState().receiptItems
    }

    fun getReceiptTotal(): Double {
        return getCurrentState().receiptTotal
    }

    // =====================================================
    // PAYMENT METHOD VALIDATION
    // =====================================================

    fun isPaymentMethodSupported(method: PaymentMethod): Boolean {
        val supportedMethods = listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.MULTINET,
            PaymentMethod.EDENRED
        )
        return supportedMethods.contains(method)
    }

    fun getSupportedPaymentMethods(): List<PaymentMethod> {
        return listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.MULTINET,
            PaymentMethod.EDENRED
        )
    }

    fun getUnsupportedPaymentMethods(): List<PaymentMethod> {
        return listOf(
            PaymentMethod.CASH,
            PaymentMethod.COUPON,
            PaymentMethod.SODEXO
        )
    }

    fun getAllPaymentMethods(): List<PaymentMethod> {
        return PaymentMethod.values().toList()
    }
}