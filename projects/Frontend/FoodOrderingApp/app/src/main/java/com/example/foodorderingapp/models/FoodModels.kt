// Replace your existing models/Models.kt file with this (FIXED VERSION):
package com.example.foodorderingapp.models

import androidx.compose.runtime.*
import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import com.example.foodorderingapp.network.*

// =====================================================
// DATA MODELS
// =====================================================

data class Category(
    val cid: Int,
    val cname: String,
    val description: String = ""
)

data class Product(
    val pid: Int,
    val cid: Int = 0,
    val pname: String,
    val price: Double,
    val description: String = "",
    val imageUrl: String = ""
)

data class CartItem(
    val product: Product,
    var quantity: Int = 1
) {
    val subtotal: Double
        get() = product.price * quantity
}

// =====================================================
// ENUMS
// =====================================================

enum class PaymentState {
    NONE, SELECTING, PROCESSING, SUCCESS, FAILED
}

enum class PaymentMethod(val displayName: String, val apiValue: String) {
    CREDIT_CARD("Credit Card", "Credit Card"),
    CASH("Cash", "Cash"),
    COUPON("Coupon", "Coupon"),
    SODEXO("Sodexo", "Sodexo"),
    MULTINET("Multinet", "Multinet"),
    EDENRED("Edenred", "Edenred")
}

enum class LoadingState {
    IDLE, LOADING, SUCCESS, ERROR
}

// =====================================================
// VIEW MODEL - DATABASE ONLY (No Mock Data)
// =====================================================

class DemoFoodOrderingViewModel(private val context: Context? = null) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // UI State - Start with empty lists (will be loaded from database)
    var products by mutableStateOf<List<Product>>(emptyList())
        private set

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    var cartItems by mutableStateOf<List<CartItem>>(emptyList())
        private set

    var showPaymentDialog by mutableStateOf(false)
        private set

    var showCartDialog by mutableStateOf(false)
        private set

    var paymentState by mutableStateOf(PaymentState.NONE)
        private set

    var selectedPaymentMethod by mutableStateOf(PaymentMethod.CREDIT_CARD)
        private set

    // API connection state
    var isConnectedToDatabase by mutableStateOf(false)
        private set

    var loadingState by mutableStateOf(LoadingState.IDLE)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var currentUserId by mutableStateOf(1)
        private set

    // Computed properties
    val cartTotal: Double
        get() = cartItems.sumOf { it.subtotal }

    val cartItemCount: Int
        get() = cartItems.sumOf { it.quantity }

    // =====================================================
    // API CONNECTION METHODS
    // =====================================================

    fun initializeConnection(serverUrl: String) {
        if (!ApiClient.isInitialized()) {
            ApiClient.initialize(serverUrl)
        }
        testConnectionAndLoadData()
    }

    private fun testConnectionAndLoadData() {
        scope.launch {
            try {
                loadingState = LoadingState.LOADING
                isConnectedToDatabase = false // Reset state
                showToast("🔄 Connecting to database...")

                Log.d("ViewModel", "=== CONNECTION ATTEMPT START ===")
                Log.d("ViewModel", "API URL: ${ApiClient.getCurrentBaseUrl()}")
                Log.d("ViewModel", "Current isConnectedToDatabase: $isConnectedToDatabase")

                // Test connection first
                val apiService = ApiClient.getApiService()
                Log.d("ViewModel", "API Service created successfully")

                val connectionResult = apiService.testConnection()
                Log.d("ViewModel", "Connection test SUCCESS: ${connectionResult.message}")

                // ÖNEMLİ: Connection başarılı olduğunda state'i güncelle
                isConnectedToDatabase = true
                Log.d("ViewModel", "isConnectedToDatabase updated to: $isConnectedToDatabase")

                showToast("✅ Connected: ${connectionResult.message}")

                // Load products from API
                Log.d("ViewModel", "Loading products...")
                val apiProducts = apiService.getProducts()
                Log.d("ViewModel", "Received ${apiProducts.size} products from API")

                products = apiProducts.map { apiProduct ->
                    Product(
                        pid = apiProduct.id,
                        pname = apiProduct.name,
                        price = apiProduct.price,
                        description = apiProduct.description,
                        imageUrl = getEmojiForProduct(apiProduct.name)
                    )
                }
                Log.d("ViewModel", "Products mapped successfully: ${products.size}")

                // Load categories from API
                Log.d("ViewModel", "Loading categories...")
                val apiCategories = apiService.getCategories()
                Log.d("ViewModel", "Received ${apiCategories.size} categories from API")

                categories = apiCategories.map { apiCategory ->
                    Category(
                        cid = apiCategory.id,
                        cname = apiCategory.name,
                        description = apiCategory.description
                    )
                }
                Log.d("ViewModel", "Categories mapped successfully: ${categories.size}")

                // Load users (to get current user ID)
                try {
                    Log.d("ViewModel", "Loading users...")
                    val apiUsers = apiService.getUsers()
                    if (apiUsers.isNotEmpty()) {
                        currentUserId = apiUsers.first().id
                        Log.d("ViewModel", "Using user ID: $currentUserId")
                    }
                } catch (e: Exception) {
                    Log.w("ViewModel", "Could not load users: ${e.message}")
                    // Users yüklenemezse default user ID kullan
                    currentUserId = 1
                    Log.d("ViewModel", "Using default user ID: $currentUserId")
                }

                // Final state update
                if (products.isNotEmpty()) {
                    loadingState = LoadingState.SUCCESS
                    Log.d("ViewModel", "LoadingState set to SUCCESS")
                    showToast("📦 Loaded ${products.size} products from database")
                    Log.i("ViewModel", "=== CONNECTION SUCCESSFUL: ${products.size} products, ${categories.size} categories ===")
                } else {
                    loadingState = LoadingState.ERROR
                    errorMessage = "No products found in database"
                    showToast("⚠️ No products found in database")
                    Log.w("ViewModel", "No products found in database")
                }

            } catch (e: java.net.ConnectException) {
                handleConnectionError(e, "Cannot connect to server. Check if backend is running and URL is correct.")
            } catch (e: java.net.UnknownHostException) {
                handleConnectionError(e, "Unknown host. Check your server URL.")
            } catch (e: java.net.SocketTimeoutException) {
                handleConnectionError(e, "Connection timeout. Server may be slow or unreachable.")
            } catch (e: retrofit2.HttpException) {
                handleConnectionError(e, "HTTP Error ${e.code()}: ${e.message()}")
            } catch (e: Exception) {
                handleConnectionError(e, "Unexpected error: ${e.javaClass.simpleName}")
            }
        }
    }

    // handleConnectionError methodunu da güncelleyin:
    private fun handleConnectionError(e: Exception, userMessage: String) {
        isConnectedToDatabase = false
        loadingState = LoadingState.ERROR
        errorMessage = "$userMessage\n\nTechnical details: ${e.message}"

        // Detailed logging
        Log.e("ViewModel", "=== CONNECTION ERROR DETAILS ===")
        Log.e("ViewModel", "Error Type: ${e.javaClass.simpleName}")
        Log.e("ViewModel", "Error Message: ${e.message}")
        Log.e("ViewModel", "API URL: ${ApiClient.getCurrentBaseUrl()}")
        Log.e("ViewModel", "User Message: $userMessage")
        Log.e("ViewModel", "isConnectedToDatabase set to: $isConnectedToDatabase")
        Log.e("ViewModel", "LoadingState set to: $loadingState")
        Log.e("ViewModel", "===============================")

        showToast("❌ $userMessage")

        // Keep lists empty
        products = emptyList()
        categories = emptyList()
    }

    // Debug için yeni method ekleyin:
    fun debugConnectionState() {
        Log.d("ViewModel", "=== CURRENT STATE DEBUG ===")
        Log.d("ViewModel", "isConnectedToDatabase: $isConnectedToDatabase")
        Log.d("ViewModel", "loadingState: $loadingState")
        Log.d("ViewModel", "products.size: ${products.size}")
        Log.d("ViewModel", "categories.size: ${categories.size}")
        Log.d("ViewModel", "errorMessage: $errorMessage")
        Log.d("ViewModel", "API initialized: ${ApiClient.isInitialized()}")
        Log.d("ViewModel", "API URL: ${ApiClient.getCurrentBaseUrl()}")
        Log.d("ViewModel", "========================")
    }

    private fun getEmojiForProduct(productName: String): String {
        return when {
            productName.contains("burger", ignoreCase = true) ||
                    productName.contains("big king", ignoreCase = true) ||
                    productName.contains("classic", ignoreCase = true) -> "🍔"

            productName.contains("cola", ignoreCase = true) ||
                    productName.contains("coke", ignoreCase = true) ||
                    productName.contains("pepsi", ignoreCase = true) -> "🥤"

            productName.contains("juice", ignoreCase = true) ||
                    productName.contains("orange", ignoreCase = true) -> "🧃"

            productName.contains("water", ignoreCase = true) -> "💧"
            productName.contains("coffee", ignoreCase = true) -> "☕"
            productName.contains("fries", ignoreCase = true) -> "🍟"
            productName.contains("onion", ignoreCase = true) -> "🧅"
            productName.contains("nuggets", ignoreCase = true) ||
                    productName.contains("chicken", ignoreCase = true) -> "🍗"
            productName.contains("cheese", ignoreCase = true) -> "🧀"

            else -> "🍽️"
        }
    }

    // =====================================================
    // CART METHODS
    // =====================================================

    fun addToCart(product: Product) {
        val existingItem = cartItems.find { it.product.pid == product.pid }
        if (existingItem != null) {
            cartItems = cartItems.map {
                if (it.product.pid == product.pid) {
                    it.copy(quantity = it.quantity + 1)
                } else it
            }
        } else {
            cartItems = cartItems + CartItem(product, 1)
        }
        showToast("➕ ${product.pname} added to cart")
    }

    fun removeFromCart(productId: Int) {
        val removedItem = cartItems.find { it.product.pid == productId }
        cartItems = cartItems.filter { it.product.pid != productId }
        removedItem?.let {
            showToast("🗑️ ${it.product.pname} removed from cart")
        }
    }

    fun updateQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
        } else {
            cartItems = cartItems.map {
                if (it.product.pid == productId) {
                    it.copy(quantity = quantity)
                } else it
            }
        }
    }

    // =====================================================
    // PAYMENT METHODS - UPDATED
    // =====================================================

    fun showPayment() {
        // Debug state
        debugConnectionState()

        // Daha esnek connection kontrolü
        if (loadingState != LoadingState.SUCCESS) {
            Log.w("ViewModel", "Payment blocked - LoadingState: $loadingState")
            showToast("⚠️ Please wait for data to load completely")
            return
        }

        if (products.isEmpty()) {
            Log.w("ViewModel", "Payment blocked - No products loaded")
            showToast("⚠️ No products loaded. Please refresh and try again.")
            return
        }

        if (cartItems.isEmpty()) {
            Log.w("ViewModel", "Payment blocked - Cart is empty")
            showToast("🛒 Your cart is empty")
            return
        }

        Log.d("ViewModel", "Payment allowed - All checks passed")
        Log.d("ViewModel", "Cart items: ${cartItems.size}, Total: $cartTotal")

        showPaymentDialog = true
        paymentState = PaymentState.SELECTING
    }

    fun hidePayment() {
        showPaymentDialog = false
        paymentState = PaymentState.NONE
    }

    fun processPayment(method: PaymentMethod) {
        // Debug state
        debugConnectionState()

        // Daha esnek connection kontrolü
        if (loadingState != LoadingState.SUCCESS || products.isEmpty()) {
            Log.e("ViewModel", "Payment processing blocked - Data not loaded properly")
            showToast("❌ Cannot process payment: Data not loaded properly")
            paymentState = PaymentState.FAILED
            return
        }

        selectedPaymentMethod = method
        paymentState = PaymentState.PROCESSING

        Log.d("ViewModel", "Starting payment processing with method: ${method.displayName}")
        Log.d("ViewModel", "Cart items: ${cartItems.size}, Total: $cartTotal")

        scope.launch {
            try {
                delay(2000) // Simulate processing time

                // Create order items - Backend'inizin beklediği format
                val orderItems = cartItems.map { cartItem ->
                    CreateOrderItemRequest(
                        productId = cartItem.product.pid,
                        quantity = cartItem.quantity
                        // unitPrice ve totalPrice backend'de hesaplanıyor
                    )
                }

                val orderRequest = CreateOrderRequest(
                    userId = currentUserId,
                    deliveryAddress = "Default Delivery Address", // Configurable yapabilirsiniz
                    paymentMethod = method.apiValue,
                    orderItems = orderItems
                )

                Log.d("ViewModel", "Sending order request:")
                Log.d("ViewModel", "- User ID: $currentUserId")
                Log.d("ViewModel", "- Payment Method: ${method.apiValue}")
                Log.d("ViewModel", "- Delivery Address: Default Delivery Address")
                Log.d("ViewModel", "- Order Items: ${orderItems.size}")

                // Log her item'ı detaylı
                orderItems.forEachIndexed { index, item ->
                    Log.d("ViewModel", "  Item $index: Product ${item.productId}, Qty ${item.quantity}")
                }

                val apiService = ApiClient.getApiService()
                Log.d("ViewModel", "Calling API endpoint: POST api/orders")

                val response = apiService.createOrder(orderRequest)

                Log.d("ViewModel", "API Response received:")
                Log.d("ViewModel", "- Success: ${response.success}")
                Log.d("ViewModel", "- Order ID: ${response.orderId}")
                Log.d("ViewModel", "- Message: ${response.message}")
                Log.d("ViewModel", "- Total Amount: ${response.totalAmount}")
                Log.d("ViewModel", "- Order Status: ${response.orderStatus}")

                if (response.success) {
                    paymentState = PaymentState.SUCCESS
                    showToast("🎉 Order created! Order ID: ${response.orderId}")
                    Log.i("ViewModel", "Order created successfully: ${response.message}, ID: ${response.orderId}")
                } else {
                    paymentState = PaymentState.FAILED
                    errorMessage = "Order creation failed: ${response.message}"
                    showToast("❌ Order failed: ${response.message}")
                    Log.e("ViewModel", "Order creation failed: ${response.message}")
                }

            } catch (e: retrofit2.HttpException) {
                paymentState = PaymentState.FAILED
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("ViewModel", "HTTP Error ${e.code()}: $errorBody")

                errorMessage = when (e.code()) {
                    400 -> "Bad request: Please check your order details"
                    404 -> "API endpoint not found: ${e.response()?.raw()?.request?.url}"
                    500 -> "Server error: Please try again later"
                    else -> "HTTP Error ${e.code()}: ${e.message()}"
                }

                showToast("❌ Payment failed: $errorMessage")
                Log.e("ViewModel", "HTTP Error details:", e)

            } catch (e: Exception) {
                paymentState = PaymentState.FAILED
                errorMessage = "Payment failed: ${e.message}"
                showToast("❌ Payment processing failed: ${e.message}")
                Log.e("ViewModel", "Payment processing failed", e)
            }
        }
    }

    fun retryPayment() {
        paymentState = PaymentState.SELECTING
    }

    fun clearCartAndHidePayment() {
        cartItems = emptyList()
        hidePayment()
        showToast("🛒 Cart cleared")
    }

    // =====================================================
    // UI METHODS
    // =====================================================

    fun showCart() {
        showCartDialog = true
    }

    fun hideCart() {
        showCartDialog = false
    }

    fun searchProducts(query: String): List<Product> {
        return if (query.isBlank()) {
            products
        } else {
            products.filter {
                it.pname.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
    }

    fun getProductsByCategory(categoryId: Int): List<Product> {
        return products.filter { it.cid == categoryId }
    }

    fun getCategoryName(categoryId: Int): String {
        return categories.find { it.cid == categoryId }?.cname ?: "All Products"
    }

    fun getCartItemsSummary(): String {
        return cartItems.joinToString(", ") { "${it.product.pname} x${it.quantity}" }
    }

    // =====================================================
    // UTILITY METHODS
    // =====================================================

    fun refreshData() {
        if (ApiClient.isInitialized()) {
            testConnectionAndLoadData()
        } else {
            showToast("⚠️ No server configured. Please configure server URL in settings.")
            loadingState = LoadingState.ERROR
            errorMessage = "No server configured"
        }
    }

    fun clearError() {
        errorMessage = null
    }

    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun onCleared() {
        scope.cancel()
    }
}

// =====================================================
// ALIAS FOR COMPATIBILITY
// =====================================================

typealias FoodOrderingViewModel = DemoFoodOrderingViewModel