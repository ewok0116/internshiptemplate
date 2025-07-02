// presentation/viewmodel/FoodOrderingViewModel.kt - COMPLETE FIXED VERSION
package com.example.foodorderingapp_ver2.presentation.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import com.example.foodorderingapp_ver2.domain.entities.*
import com.example.foodorderingapp_ver2.domain.usecases.*
import com.example.foodorderingapp_ver2.domain.common.Result

// UI State Classes
data class CartItemUi(
    val product: Product,
    var quantity: Int = 1
) {
    val subtotal: Double get() = product.price * quantity
}

enum class PaymentStateUi {
    NONE, SELECTING, PROCESSING, SUCCESS, FAILED
}

enum class LoadingStateUi {
    IDLE, LOADING, SUCCESS, ERROR
}

data class FoodOrderingUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val cartItems: List<CartItemUi> = emptyList(),
    val completedOrderItems: List<CartItemUi> = emptyList(), // NEW: For receipt display
    val showPaymentDialog: Boolean = false,
    val showCartDialog: Boolean = false,
    val paymentState: PaymentStateUi = PaymentStateUi.NONE,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val isConnectedToDatabase: Boolean = false,
    val loadingState: LoadingStateUi = LoadingStateUi.IDLE,
    val errorMessage: String? = null,
    val currentUserId: Int = 1,
    val lastOrderId: Int? = null // NEW: Track the last successful order
) {
    val cartTotal: Double get() = cartItems.sumOf { it.subtotal }
    val cartItemCount: Int get() = cartItems.sumOf { it.quantity }

    // NEW: For receipt - use completed order items if available, otherwise current cart
    val receiptItems: List<CartItemUi> get() = if (completedOrderItems.isNotEmpty()) completedOrderItems else cartItems
    val receiptTotal: Double get() = receiptItems.sumOf { it.subtotal }
}

// Payment result data class
data class PaymentResult(
    val isSuccess: Boolean,
    val transactionId: String? = null,
    val errorMessage: String? = null
)

class FoodOrderingViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val testConnectionUseCase: TestConnectionUseCase,
    private val initializeConnectionUseCase: InitializeConnectionUseCase,
    private val onShowToast: (String) -> Unit = {}
) : ViewModel() {

    var uiState by mutableStateOf(FoodOrderingUiState())
        private set

    // =====================================================
    // CONNECTION METHODS
    // =====================================================

    fun initializeConnection(serverUrl: String) {
        viewModelScope.launch {
            uiState = uiState.copy(loadingState = LoadingStateUi.LOADING)
            onShowToast("🔄 Connecting to database...")

            when (val result = initializeConnectionUseCase(serverUrl)) {
                is Result.Success -> {
                    uiState = uiState.copy(isConnectedToDatabase = true)
                    onShowToast("✅ Connection successful")
                    loadAllData()
                }

                is Result.Error -> {
                    handleConnectionError(result.message)
                }

                Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    private suspend fun loadAllData() {
        try {
            // Load products and categories in parallel
            val productsResult = getProductsUseCase()
            val categoriesResult = getCategoriesUseCase()

            when (productsResult) {
                is Result.Success -> {
                    uiState = uiState.copy(products = productsResult.data)
                    Log.d("ViewModel", "Loaded ${productsResult.data.size} products")
                }

                is Result.Error -> {
                    Log.e("ViewModel", "Failed to load products: ${productsResult.message}")
                    uiState = uiState.copy(errorMessage = productsResult.message)
                }

                Result.Loading -> {}
            }

            when (categoriesResult) {
                is Result.Success -> {
                    uiState = uiState.copy(categories = categoriesResult.data)
                    Log.d("ViewModel", "Loaded ${categoriesResult.data.size} categories")
                }

                is Result.Error -> {
                    Log.e("ViewModel", "Failed to load categories: ${categoriesResult.message}")
                    // Categories are optional, so we don't fail the entire load
                }

                Result.Loading -> {}
            }

            if (uiState.products.isNotEmpty()) {
                uiState = uiState.copy(loadingState = LoadingStateUi.SUCCESS)
                onShowToast("📦 Data loaded successfully")
            } else {
                uiState = uiState.copy(
                    loadingState = LoadingStateUi.ERROR,
                    errorMessage = "No products found in database"
                )
            }

        } catch (e: Exception) {
            Log.e("ViewModel", "Error loading data", e)
            uiState = uiState.copy(
                loadingState = LoadingStateUi.ERROR,
                errorMessage = "Failed to load data: ${e.message}"
            )
        }
    }

    private fun handleConnectionError(message: String) {
        uiState = uiState.copy(
            isConnectedToDatabase = false,
            loadingState = LoadingStateUi.ERROR,
            errorMessage = message,
            products = emptyList(),
            categories = emptyList()
        )
        onShowToast("❌ $message")
    }

    fun loadDataDirectly() {
        viewModelScope.launch {
            uiState = uiState.copy(
                loadingState = LoadingStateUi.LOADING,
                isConnectedToDatabase = true // We know connection is already established
            )

            try {
                // Since connection is already established and tested, directly load data
                loadAllData()
            } catch (e: Exception) {
                Log.e("ViewModel", "Error loading data directly", e)
                uiState = uiState.copy(
                    loadingState = LoadingStateUi.ERROR,
                    errorMessage = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    // =====================================================
    // CART METHODS
    // =====================================================

    fun addToCart(product: Product) {
        val existingItem = uiState.cartItems.find { it.product.id == product.id }
        val updatedCartItems = if (existingItem != null) {
            uiState.cartItems.map {
                if (it.product.id == product.id) {
                    it.copy(quantity = it.quantity + 1)
                } else it
            }
        } else {
            uiState.cartItems + CartItemUi(product, 1)
        }

        uiState = uiState.copy(cartItems = updatedCartItems)
        onShowToast("➕ ${product.name} added to cart")
    }

    fun removeFromCart(productId: Int) {
        val removedItem = uiState.cartItems.find { it.product.id == productId }
        val updatedCartItems = uiState.cartItems.filter { it.product.id != productId }

        uiState = uiState.copy(cartItems = updatedCartItems)
        removedItem?.let {
            onShowToast("🗑️ ${it.product.name} removed from cart")
        }
    }

    fun updateQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
        } else {
            val updatedCartItems = uiState.cartItems.map {
                if (it.product.id == productId) {
                    it.copy(quantity = quantity)
                } else it
            }
            uiState = uiState.copy(cartItems = updatedCartItems)
        }
    }

    // =====================================================
    // PAYMENT METHODS - FIXED IMPLEMENTATION
    // =====================================================

    fun showPayment() {
        if (uiState.loadingState != LoadingStateUi.SUCCESS) {
            onShowToast("⚠️ Please wait for data to load completely")
            return
        }

        if (uiState.cartItems.isEmpty()) {
            onShowToast("🛒 Your cart is empty")
            return
        }

        uiState = uiState.copy(
            showPaymentDialog = true,
            paymentState = PaymentStateUi.SELECTING
        )
    }

    fun hidePayment() {
        Log.d("ViewModel", "🚨 hidePayment() called")
        Log.d("ViewModel", "    Current paymentState = ${uiState.paymentState}")

        // Don't reset state if payment was successful - let user see success dialog!
        if (uiState.paymentState == PaymentStateUi.SUCCESS) {
            Log.d("ViewModel", "⚠️ Keeping SUCCESS state - just hiding dialog")
            uiState = uiState.copy(showPaymentDialog = false)
        } else {
            Log.d("ViewModel", "✅ Resetting payment state to NONE")
            uiState = uiState.copy(
                showPaymentDialog = false,
                paymentState = PaymentStateUi.NONE
            )
        }
    }

    fun processPayment(method: PaymentMethod) {
        Log.d("ViewModel", "🎯 Starting payment process with method: ${method.displayName}")

        // Basic validation
        if (uiState.loadingState != LoadingStateUi.SUCCESS || uiState.products.isEmpty()) {
            Log.e("ViewModel", "❌ Cannot process payment: LoadingState=${uiState.loadingState}, Products=${uiState.products.size}")
            onShowToast("❌ Cannot process payment: Data not loaded properly")
            uiState = uiState.copy(paymentState = PaymentStateUi.FAILED)
            return
        }

        if (uiState.cartItems.isEmpty()) {
            Log.e("ViewModel", "❌ Cannot process payment: Cart is empty")
            onShowToast("❌ Cart is empty")
            uiState = uiState.copy(paymentState = PaymentStateUi.FAILED)
            return
        }

        Log.d("ViewModel", "✅ Pre-payment validation passed")
        Log.d("ViewModel", "📦 Cart items: ${uiState.cartItems.size}")
        Log.d("ViewModel", "💰 Cart total: ${uiState.cartTotal}")

        uiState = uiState.copy(
            selectedPaymentMethod = method,
            paymentState = PaymentStateUi.PROCESSING
        )

        viewModelScope.launch {
            try {
                // *** STEP 1: PROCESS PAYMENT FIRST ***
                Log.d("ViewModel", "💳 Step 1: Processing payment BEFORE creating order...")

                val paymentResult = processPaymentWithProvider(method, uiState.cartTotal)

                Log.d("ViewModel", "💳 Step 2: Payment result received:")
                Log.d("ViewModel", "    isSuccess = ${paymentResult.isSuccess}")
                Log.d("ViewModel", "    transactionId = ${paymentResult.transactionId}")
                Log.d("ViewModel", "    errorMessage = ${paymentResult.errorMessage}")

                if (paymentResult.isSuccess) {
                    Log.i("ViewModel", "💳 ✅ Payment successful! Transaction ID: ${paymentResult.transactionId}")
                    Log.i("ViewModel", "💳 ✅ Now creating order in database...")

                    // *** STEP 2: CREATE ORDER IN DATABASE ***
                    val cartItems = uiState.cartItems.map { cartItemUi ->
                        Log.d("ViewModel", "📝 Mapping cart item: ${cartItemUi.product.name} x${cartItemUi.quantity} = ${cartItemUi.subtotal} TL")
                        CartItem(
                            product = cartItemUi.product,
                            quantity = cartItemUi.quantity
                        )
                    }

                    val createOrderParams = CreateOrderParams(
                        userId = uiState.currentUserId,
                        items = cartItems,
                        deliveryAddress = "Default Delivery Address",
                        paymentMethod = method
                    )

                    Log.d("ViewModel", "🌐 Calling createOrderUseCase...")
                    Log.d("ViewModel", "🔍 DEBUG: createOrderParams details:")
                    Log.d("ViewModel", "    userId = ${createOrderParams.userId}")
                    Log.d("ViewModel", "    items count = ${createOrderParams.items.size}")
                    Log.d("ViewModel", "    deliveryAddress = ${createOrderParams.deliveryAddress}")
                    Log.d("ViewModel", "    paymentMethod = ${createOrderParams.paymentMethod}")

                    when (val orderResult = createOrderUseCase(createOrderParams)) {
                        is Result.Success -> {
                            Log.i("ViewModel", "🎉 SUCCESS! Order created in database:")
                            Log.i("ViewModel", "   - Order ID: ${orderResult.data.id}")
                            Log.i("ViewModel", "   - Payment method: ${method.displayName}")
                            Log.i("ViewModel", "   - Transaction ID: ${paymentResult.transactionId}")

                            // Set SUCCESS state
                            uiState = uiState.copy(
                                paymentState = PaymentStateUi.SUCCESS,
                                completedOrderItems = uiState.cartItems.toList(), // Save for receipt
                                lastOrderId = orderResult.data.id,
                                cartItems = emptyList() // Clear cart
                            )

                            Log.d("ViewModel", "✅ SUCCESS STATE SET!")
                            Log.d("ViewModel", "    paymentState = ${uiState.paymentState}")
                            Log.d("ViewModel", "    lastOrderId = ${uiState.lastOrderId}")

                            onShowToast("🎉 Payment successful! Order #${orderResult.data.id} saved to database!")
                        }

                        is Result.Error -> {
                            Log.e("ViewModel", "❌ Order creation failed:")
                            Log.e("ViewModel", "    Error: ${orderResult.message}")
                            Log.i("ViewModel", "⚠️ Payment succeeded but order failed - STILL SHOWING SUCCESS")

                            // Payment succeeded, so still show success even if order creation failed
                            val fakeOrderId = (1000..9999).random()
                            uiState = uiState.copy(
                                paymentState = PaymentStateUi.SUCCESS,
                                completedOrderItems = uiState.cartItems.toList(),
                                lastOrderId = fakeOrderId,
                                cartItems = emptyList()
                            )

                            Log.d("ViewModel", "✅ SUCCESS STATE SET (despite order creation failure)!")
                            Log.d("ViewModel", "    paymentState = ${uiState.paymentState}")
                            Log.d("ViewModel", "    fakeOrderId = $fakeOrderId")

                            onShowToast("🎉 Payment successful! (Order #$fakeOrderId - DB save failed but payment processed)")
                        }

                        Result.Loading -> {
                            Log.d("ViewModel", "⏳ Order creation still loading after payment success...")
                        }
                    }
                } else {
                    Log.e("ViewModel", "💳 ❌ Payment failed with error: ${paymentResult.errorMessage}")
                    Log.d("ViewModel", "🚫 Order will NOT be created - payment failed")

                    uiState = uiState.copy(
                        paymentState = PaymentStateUi.FAILED,
                        errorMessage = paymentResult.errorMessage
                    )
                    Log.d("ViewModel", "❌ FAILED STATE SET due to payment error")
                    onShowToast("❌ Payment failed: ${paymentResult.errorMessage}")
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "❌ Exception in payment processing:", e)

                uiState = uiState.copy(
                    paymentState = PaymentStateUi.FAILED,
                    errorMessage = "Payment processing exception: ${e.message}"
                )
                Log.d("ViewModel", "❌ FAILED STATE SET due to exception")
                onShowToast("❌ Payment processing failed: ${e.message}")
            }
        }
    }

    // *** FIXED PAYMENT PROVIDER - ONLY ALLOW CREDIT CARD, MULTINET, EDENRED ***
    private suspend fun processPaymentWithProvider(method: PaymentMethod, amount: Double): PaymentResult {
        Log.d("ViewModel", "💳 🔄 Contacting payment provider...")
        Log.d("ViewModel", "💳 Payment details:")
        Log.d("ViewModel", "   - Method: ${method.displayName}")
        Log.d("ViewModel", "   - Amount: $amount TL")

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
            Log.i("ViewModel", "💳 ✅ Payment provider APPROVED payment:")
            Log.i("ViewModel", "   - Method: ${method.displayName}")
            Log.i("ViewModel", "   - Amount: $amount TL")
            Log.i("ViewModel", "   - Transaction ID: $transactionId")

            PaymentResult(isSuccess = true, transactionId = transactionId)
        } else {
            // FAILURE for Cash, Coupon, and Sodexo
            val errorMsg = "Payment method '${method.displayName}' is not accepted. Only Credit Card, Multinet, and Edenred are supported."
            Log.e("ViewModel", "💳 ❌ Payment provider REJECTED payment:")
            Log.e("ViewModel", "   - Method: ${method.displayName}")
            Log.e("ViewModel", "   - Reason: Method not supported")
            Log.e("ViewModel", "   - Allowed methods: Credit Card, Multinet, Edenred")

            PaymentResult(isSuccess = false, errorMessage = errorMsg)
        }
    }

    fun retryPayment() {
        uiState = uiState.copy(paymentState = PaymentStateUi.SELECTING)
    }

    // Clear completed order data when going home
    fun clearCartAndHidePayment() {
        Log.d("ViewModel", "🧹 User chose to clear cart and go home")

        uiState = uiState.copy(
            cartItems = emptyList(),
            completedOrderItems = emptyList(), // Clear receipt data too
            showPaymentDialog = false,
            paymentState = PaymentStateUi.NONE,
            lastOrderId = null
        )
        Log.d("ViewModel", "🏠 Returning to home - all order data cleared")
        onShowToast("🏠 Returning to home")
    }

    // =====================================================
    // UI METHODS
    // =====================================================

    fun showCart() {
        uiState = uiState.copy(showCartDialog = true)
    }

    fun hideCart() {
        uiState = uiState.copy(showCartDialog = false)
    }

    fun searchProducts(query: String, callback: (List<Product>) -> Unit) {
        viewModelScope.launch {
            when (val result = searchProductsUseCase(query)) {
                is Result.Success -> callback(result.data)
                is Result.Error -> {
                    Log.e("ViewModel", "Search failed: ${result.message}")
                    callback(emptyList())
                }

                Result.Loading -> {}
            }
        }
    }

    fun getProductsByCategory(categoryId: Int, callback: (List<Product>) -> Unit) {
        viewModelScope.launch {
            when (val result = getProductsByCategoryUseCase(categoryId)) {
                is Result.Success -> callback(result.data)
                is Result.Error -> {
                    Log.e("ViewModel", "Category filter failed: ${result.message}")
                    callback(emptyList())
                }

                Result.Loading -> {}
            }
        }
    }

    fun getCategoryName(categoryId: Int): String {
        return uiState.categories.find { it.id == categoryId }?.name ?: "All Products"
    }

    // Use receipt items for summary
    fun getCartItemsSummary(): String {
        return uiState.receiptItems.joinToString(", ") { "${it.product.name} x${it.quantity}" }
    }

    // =====================================================
    // UTILITY METHODS
    // =====================================================

    fun refreshData() {
        viewModelScope.launch {
            uiState = uiState.copy(loadingState = LoadingStateUi.LOADING)

            when (val result = testConnectionUseCase()) {
                is Result.Success -> {
                    loadAllData()
                }

                is Result.Error -> {
                    handleConnectionError(result.message)
                }

                Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}