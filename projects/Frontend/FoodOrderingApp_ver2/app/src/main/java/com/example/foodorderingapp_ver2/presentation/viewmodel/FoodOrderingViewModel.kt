// presentation/viewmodel/FoodOrderingViewModel.kt - WITH PAYMENT MANAGER INTEGRATION
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
    val completedOrderItems: List<CartItemUi> = emptyList(), // For receipt display
    val showPaymentDialog: Boolean = false,
    val showCartDialog: Boolean = false,
    val paymentState: PaymentStateUi = PaymentStateUi.NONE,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val isConnectedToDatabase: Boolean = false,
    val loadingState: LoadingStateUi = LoadingStateUi.IDLE,
    val errorMessage: String? = null,
    val currentUserId: Int = 1,
    val lastOrderId: Int? = null // Track the last successful order
) {
    val cartTotal: Double get() = cartItems.sumOf { it.subtotal }
    val cartItemCount: Int get() = cartItems.sumOf { it.quantity }

    // For receipt - use completed order items if available, otherwise current cart
    val receiptItems: List<CartItemUi> get() = if (completedOrderItems.isNotEmpty()) completedOrderItems else cartItems
    val receiptTotal: Double get() = receiptItems.sumOf { it.subtotal }
}

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
    // MANAGERS SETUP
    // =====================================================

    private val connectionManager = ConnectionManager(
        initializeConnectionUseCase = initializeConnectionUseCase,
        testConnectionUseCase = testConnectionUseCase,
        getProductsUseCase = getProductsUseCase,
        getCategoriesUseCase = getCategoriesUseCase,
        coroutineScope = viewModelScope,
        onShowToast = onShowToast,
        onStateUpdate = { newState -> uiState = newState },
        getCurrentState = { uiState }
    )

    private val cartManager = CartManager(
        onShowToast = onShowToast,
        onStateUpdate = { newState -> uiState = newState },
        getCurrentState = { uiState }
    )

    private val productManager = ProductManager(
        searchProductsUseCase = searchProductsUseCase,
        getProductsByCategoryUseCase = getProductsByCategoryUseCase,
        coroutineScope = viewModelScope,
        getCurrentState = { uiState }
    )

    private val paymentManager = PaymentManager(
        createOrderUseCase = createOrderUseCase,
        coroutineScope = viewModelScope,
        onShowToast = onShowToast,
        onStateUpdate = { newState -> uiState = newState },
        getCurrentState = { uiState }
    )

    // =====================================================
    // CONNECTION METHODS - DELEGATED TO CONNECTION MANAGER
    // =====================================================

    fun initializeConnection(serverUrl: String) {
        connectionManager.initializeConnection(serverUrl)
    }

    fun loadDataDirectly() {
        connectionManager.loadDataDirectly()
    }

    fun refreshData() {
        connectionManager.refreshData()
    }

    // Helper methods to access connection status
    fun isConnectedToDatabase(): Boolean = connectionManager.isConnected()
    fun isDataLoaded(): Boolean = connectionManager.isDataLoaded()
    fun getConnectionStatus(): String = connectionManager.getConnectionStatus()

    // =====================================================
    // CART METHODS - DELEGATED TO CART MANAGER
    // =====================================================

    fun addToCart(product: Product) {
        cartManager.addToCart(product)
    }

    fun removeFromCart(productId: Int) {
        cartManager.removeFromCart(productId)
    }

    fun updateQuantity(productId: Int, quantity: Int) {
        cartManager.updateQuantity(productId, quantity)
    }

    fun showCart() {
        cartManager.showCart()
    }

    fun hideCart() {
        cartManager.hideCart()
    }

    fun getCartItemsSummary(): String {
        return cartManager.getCartItemsSummary()
    }

    // =====================================================
    // PRODUCT METHODS - DELEGATED TO PRODUCT MANAGER
    // =====================================================

    fun searchProducts(query: String, callback: (List<Product>) -> Unit) {
        productManager.searchProducts(query, callback)
    }

    fun getProductsByCategory(categoryId: Int, callback: (List<Product>) -> Unit) {
        productManager.getProductsByCategory(categoryId, callback)
    }

    fun getCategoryName(categoryId: Int): String {
        return productManager.getCategoryName(categoryId)
    }

    // Additional product helper methods - DELEGATED TO PRODUCT MANAGER
    fun getAllProducts(): List<Product> = productManager.getAllProducts()
    fun getAllCategories(): List<Category> = productManager.getAllCategories()
    fun getProductById(productId: Int): Product? = productManager.getProductById(productId)
    fun getCategoryById(categoryId: Int): Category? = productManager.getCategoryById(categoryId)

    // Product filtering methods - DELEGATED TO PRODUCT MANAGER
    fun filterProductsByName(searchTerm: String): List<Product> = productManager.filterProductsByName(searchTerm)
    fun filterProductsByPriceRange(minPrice: Double, maxPrice: Double): List<Product> = productManager.filterProductsByPriceRange(minPrice, maxPrice)
    fun getProductsInCategory(categoryId: Int): List<Product> = productManager.getProductsInCategory(categoryId)

    // Product statistics - DELEGATED TO PRODUCT MANAGER
    fun getProductCount(): Int = productManager.getProductCount()
    fun getCategoryCount(): Int = productManager.getCategoryCount()
    fun getProductCountByCategory(categoryId: Int): Int = productManager.getProductCountByCategory(categoryId)
    fun getAveragePrice(): Double = productManager.getAveragePrice()
    fun getPriceRange(): Pair<Double, Double> = productManager.getPriceRange()

    // Product validation - DELEGATED TO PRODUCT MANAGER
    fun isProductDataLoaded(): Boolean = productManager.isProductDataLoaded()
    fun isCategoryDataLoaded(): Boolean = productManager.isCategoryDataLoaded()
    fun isValidCategoryId(categoryId: Int): Boolean = productManager.isValidCategoryId(categoryId)
    fun isValidProductId(productId: Int): Boolean = productManager.isValidProductId(productId)

    // Advanced search - DELEGATED TO PRODUCT MANAGER
    fun searchProductsAdvanced(
        query: String? = null,
        categoryId: Int? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null
    ): List<Product> = productManager.searchProductsAdvanced(query, categoryId, minPrice, maxPrice)

    // =====================================================
    // PAYMENT METHODS - DELEGATED TO PAYMENT MANAGER
    // =====================================================

    fun showPayment() {
        paymentManager.showPayment()
    }

    fun hidePayment() {
        paymentManager.hidePayment()
    }

    fun processPayment(method: PaymentMethod) {
        paymentManager.processPayment(method)
    }

    fun retryPayment() {
        paymentManager.retryPayment()
    }

    fun clearCartAndHidePayment() {
        // This method needs to coordinate between cart and payment managers
        cartManager.clearCartAndHidePayment()
        val currentState = uiState
        uiState = currentState.copy(
            completedOrderItems = emptyList(),
            showPaymentDialog = false,
            paymentState = PaymentStateUi.NONE,
            lastOrderId = null
        )
        onShowToast("🏠 Welcome back!")
    }

    // Payment status methods - DELEGATED TO PAYMENT MANAGER
    fun isPaymentInProgress(): Boolean = paymentManager.isPaymentInProgress()
    fun isPaymentSuccessful(): Boolean = paymentManager.isPaymentSuccessful()
    fun isPaymentFailed(): Boolean = paymentManager.isPaymentFailed()
    fun getPaymentStatus(): PaymentStateUi = paymentManager.getPaymentStatus()
    fun getSelectedPaymentMethod(): PaymentMethod = paymentManager.getSelectedPaymentMethod()
    fun getLastOrderId(): Int? = paymentManager.getLastOrderId()
    fun getReceiptItems(): List<CartItemUi> = paymentManager.getReceiptItems()
    fun getReceiptTotal(): Double = paymentManager.getReceiptTotal()

    // Payment method validation - DELEGATED TO PAYMENT MANAGER
    fun isPaymentMethodSupported(method: PaymentMethod): Boolean = paymentManager.isPaymentMethodSupported(method)
    fun getSupportedPaymentMethods(): List<PaymentMethod> = paymentManager.getSupportedPaymentMethods()
    fun getUnsupportedPaymentMethods(): List<PaymentMethod> = paymentManager.getUnsupportedPaymentMethods()
    fun getAllPaymentMethods(): List<PaymentMethod> = paymentManager.getAllPaymentMethods()

    // =====================================================
    // UTILITY METHODS
    // =====================================================

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }
}