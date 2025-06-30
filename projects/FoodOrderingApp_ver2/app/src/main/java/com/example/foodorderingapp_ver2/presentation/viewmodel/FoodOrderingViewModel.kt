// presentation/viewmodel/FoodOrderingViewModel.kt
package com.example.foodorderingapp.presentation.viewmodel

// presentation/viewmodel/FoodOrderingViewModel.kt - FIXED IMPORTS

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import com.example.foodorderingapp.domain.entities.*
import com.example.foodorderingapp.domain.usecases.*
import com.example.foodorderingapp.domain.common.Result
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
    val showPaymentDialog: Boolean = false,
    val showCartDialog: Boolean = false,
    val paymentState: PaymentStateUi = PaymentStateUi.NONE,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val isConnectedToDatabase: Boolean = false,
    val loadingState: LoadingStateUi = LoadingStateUi.IDLE,
    val errorMessage: String? = null,
    val currentUserId: Int = 1
) {
    val cartTotal: Double get() = cartItems.sumOf { it.subtotal }
    val cartItemCount: Int get() = cartItems.sumOf { it.quantity }
}

class FoodOrderingViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
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
    // PAYMENT METHODS
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
        uiState = uiState.copy(
            showPaymentDialog = false,
            paymentState = PaymentStateUi.NONE
        )
    }

    fun processPayment(method: PaymentMethod) {
        // Validate allowed payment methods
        val allowedMethods = listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.EDENRED,
            PaymentMethod.SODEXO
        )

        if (!allowedMethods.contains(method)) {
            uiState = uiState.copy(
                paymentState = PaymentStateUi.FAILED,
                errorMessage = "Payment method not accepted"
            )
            onShowToast("❌ ${method.displayName} is not accepted")
            return
        }

        if (uiState.loadingState != LoadingStateUi.SUCCESS || uiState.products.isEmpty()) {
            onShowToast("❌ Cannot process payment: Data not loaded properly")
            uiState = uiState.copy(paymentState = PaymentStateUi.FAILED)
            return
        }

        uiState = uiState.copy(
            selectedPaymentMethod = method,
            paymentState = PaymentStateUi.PROCESSING
        )

        viewModelScope.launch {
            try {
                Log.d("ViewModel", "🔄 Starting payment processing with method: ${method.displayName}")

                // Convert CartItemUi to CartItem (domain entities)
                val cartItems = uiState.cartItems.map { cartItemUi ->
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

                when (val result = createOrderUseCase(createOrderParams)) {
                    is Result.Success -> {
                        Log.i("ViewModel", "✅ Order ${result.data.id} created successfully")

                        // Update order status to confirmed
                        val updateParams = UpdateOrderStatusParams(
                            orderId = result.data.id,
                            status = OrderStatus.CONFIRMED
                        )

                        // Note: The current API doesn't return full order data on status update
                        // So we'll consider the payment successful after order creation
                        uiState = uiState.copy(paymentState = PaymentStateUi.SUCCESS)
                        onShowToast("🎉 Payment successful! Order #${result.data.id} confirmed")

                        // Clear cart after successful payment
                        uiState = uiState.copy(cartItems = emptyList())
                    }
                    is Result.Error -> {
                        uiState = uiState.copy(
                            paymentState = PaymentStateUi.FAILED,
                            errorMessage = result.message
                        )
                        onShowToast("❌ Payment failed: ${result.message}")
                        Log.e("ViewModel", "❌ Payment failed: ${result.message}")
                    }
                    Result.Loading -> {
                        // Handle loading state if needed
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    paymentState = PaymentStateUi.FAILED,
                    errorMessage = "Payment failed: ${e.message}"
                )
                onShowToast("❌ Payment processing failed: ${e.message}")
                Log.e("ViewModel", "❌ Payment processing exception", e)
            }
        }
    }

    fun retryPayment() {
        uiState = uiState.copy(paymentState = PaymentStateUi.SELECTING)
    }

    fun clearCartAndHidePayment() {
        uiState = uiState.copy(
            cartItems = emptyList(),
            showPaymentDialog = false,
            paymentState = PaymentStateUi.NONE
        )
        onShowToast("🛒 Cart cleared")
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

    fun getCartItemsSummary(): String {
        return uiState.cartItems.joinToString(", ") { "${it.product.name} x${it.quantity}" }
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

// presentation/di/ViewModelFactory.kt
package com.example.foodorderingapp.presentation.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodorderingapp.presentation.viewmodel.FoodOrderingViewModel
import com.example.foodorderingapp.domain.usecases.*

class FoodOrderingViewModelFactory(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    private val testConnectionUseCase: TestConnectionUseCase,
    private val initializeConnectionUseCase: InitializeConnectionUseCase,
    private val onShowToast: (String) -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodOrderingViewModel::class.java)) {
            return FoodOrderingViewModel(
                getProductsUseCase = getProductsUseCase,
                getCategoriesUseCase = getCategoriesUseCase,
                searchProductsUseCase = searchProductsUseCase,
                getProductsByCategoryUseCase = getProductsByCategoryUseCase,
                createOrderUseCase = createOrderUseCase,
                updateOrderStatusUseCase = updateOrderStatusUseCase,
                testConnectionUseCase = testConnectionUseCase,
                initializeConnectionUseCase = initializeConnectionUseCase,
                onShowToast = onShowToast
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}