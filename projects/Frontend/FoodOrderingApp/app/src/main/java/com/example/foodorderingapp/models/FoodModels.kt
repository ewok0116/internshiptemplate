// models/Models.kt - COMPLETE FIXED VERSION
package com.example.foodorderingapp.models

import androidx.compose.runtime.*
import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import com.example.foodorderingapp.network.*
import retrofit2.Response

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
// VIEW MODEL - COMPLETE VERSION
// =====================================================

class DemoFoodOrderingViewModel(private val context: Context? = null) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // UI State
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
    var isConnectedToDatabase by mutableStateOf(false)
        private set
    var loadingState by mutableStateOf(LoadingState.IDLE)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var currentUserId by mutableStateOf(1)
        private set

    // Computed properties
    val cartTotal: Double get() = cartItems.sumOf { it.subtotal }
    val cartItemCount: Int get() = cartItems.sumOf { it.quantity }

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
                isConnectedToDatabase = false
                showToast("🔄 Connecting to database...")

                val apiService = ApiClient.getApiService()

                // Test connection
                val connectionResponse = apiService.testConnection()
                if (!connectionResponse.isSuccessful) {
                    handleConnectionError(Exception("Connection failed"), "Connection test failed")
                    return@launch
                }

                isConnectedToDatabase = true
                showToast("✅ Connection successful")

                // Load all data in parallel
                val productsDeferred = async { loadProducts(apiService) }
                val categoriesDeferred = async { loadCategories(apiService) }
                val usersDeferred = async { loadCurrentUser(apiService) }

                // Wait for all requests to complete
                val productsSuccess = productsDeferred.await()
                val categoriesSuccess = categoriesDeferred.await()
                usersDeferred.await()

                if (productsSuccess && categoriesSuccess) {
                    loadingState = LoadingState.SUCCESS
                    showToast("📦 Data loaded successfully")
                } else {
                    loadingState = LoadingState.ERROR
                    errorMessage = "Failed to load all data"
                }
            } catch (e: Exception) {
                handleConnectionError(e, when (e) {
                    is java.net.ConnectException -> "Cannot connect to server"
                    is java.net.UnknownHostException -> "Invalid server URL"
                    is java.net.SocketTimeoutException -> "Connection timeout"
                    is retrofit2.HttpException -> "HTTP Error ${e.code()}"
                    else -> "Connection error"
                })
            }
        }
    }
// Replace the loading methods in your Models.kt with these versions for wrapper responses

    private suspend fun loadProducts(apiService: FoodOrderingApiService): Boolean {
        return try {
            Log.d("ViewModel", "Starting to load products...")
            val response = apiService.getProducts()

            Log.d("ViewModel", "Products response code: ${response.code()}")
            Log.d("ViewModel", "Products response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("ViewModel", "Products response body: $apiResponse")

                    // Check if the wrapper response is successful
                    if (apiResponse.success) {
                        apiResponse.data?.let { apiProducts ->
                            Log.d("ViewModel", "Found ${apiProducts.size} products in wrapper response")

                            if (apiProducts.isNotEmpty()) {
                                products = apiProducts.map { apiProduct ->
                                    Product(
                                        pid = apiProduct.id,
                                        pname = apiProduct.name,
                                        price = apiProduct.price,
                                        description = apiProduct.description,
                                        cid = apiProduct.categoryId ?: 0,
                                        imageUrl = getEmojiForProduct(apiProduct.name)
                                    )
                                }
                                Log.d("ViewModel", "Successfully mapped ${products.size} products")
                                true
                            } else {
                                Log.w("ViewModel", "Products array is empty")
                                errorMessage = "No products found in database"
                                false
                            }
                        } ?: run {
                            Log.e("ViewModel", "Products data is null in wrapper")
                            errorMessage = "No products data in response"
                            false
                        }
                    } else {
                        Log.e("ViewModel", "API returned success=false: ${apiResponse.message}")
                        errorMessage = apiResponse.message ?: "Failed to load products"
                        false
                    }
                } ?: run {
                    Log.e("ViewModel", "Products response body is null")
                    errorMessage = "Empty response from server"
                    false
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("ViewModel", "Products HTTP error ${response.code()}: $errorBody")
                errorMessage = "HTTP ${response.code()}: ${response.message()}"
                false
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "Products loading exception", e)
            errorMessage = "Products error: ${e.message}"
            false
        }
    }

    private suspend fun loadCategories(apiService: FoodOrderingApiService): Boolean {
        return try {
            Log.d("ViewModel", "Starting to load categories...")
            val response = apiService.getCategories()

            Log.d("ViewModel", "Categories response code: ${response.code()}")
            Log.d("ViewModel", "Categories response successful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("ViewModel", "Categories response body: $apiResponse")

                    // Check if the wrapper response is successful
                    if (apiResponse.success) {
                        apiResponse.data?.let { apiCategories ->
                            Log.d("ViewModel", "Found ${apiCategories.size} categories in wrapper response")

                            categories = apiCategories.map { apiCategory ->
                                Category(
                                    cid = apiCategory.id,
                                    cname = apiCategory.name,
                                    description = apiCategory.description
                                )
                            }
                            Log.d("ViewModel", "Successfully mapped ${categories.size} categories")
                            true
                        } ?: run {
                            Log.w("ViewModel", "Categories data is null in wrapper")
                            // Even if no categories, still return true (categories are optional)
                            categories = emptyList()
                            true
                        }
                    } else {
                        Log.e("ViewModel", "API returned success=false for categories: ${apiResponse.message}")
                        errorMessage = apiResponse.message ?: "Failed to load categories"
                        // Categories are optional, so we can still continue
                        categories = emptyList()
                        true
                    }
                } ?: run {
                    Log.e("ViewModel", "Categories response body is null")
                    errorMessage = "Empty categories response from server"
                    false
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("ViewModel", "Categories HTTP error ${response.code()}: $errorBody")
                errorMessage = "HTTP ${response.code()}: ${response.message()}"
                false
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "Categories loading exception", e)
            errorMessage = "Categories error: ${e.message}"
            false
        }
    }

    private suspend fun loadCurrentUser(apiService: FoodOrderingApiService) {
        try {
            Log.d("ViewModel", "Starting to load users...")
            val response = apiService.getUsers()

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    Log.d("ViewModel", "Users response body: $apiResponse")

                    if (apiResponse.success) {
                        apiResponse.data?.let { apiUsers ->
                            Log.d("ViewModel", "Found ${apiUsers.size} users in wrapper response")

                            if (apiUsers.isNotEmpty()) {
                                val user = apiUsers.first()
                                currentUserId = user.id
                                Log.d("ViewModel", "Set current user ID to: $currentUserId")
                            } else {
                                Log.w("ViewModel", "No users found in data, using default ID")
                                currentUserId = 1
                            }
                        } ?: run {
                            Log.w("ViewModel", "Users data is null in wrapper, using default ID")
                            currentUserId = 1
                        }
                    } else {
                        Log.w("ViewModel", "API returned success=false for users: ${apiResponse.message}, using default ID")
                        currentUserId = 1
                    }
                } ?: run {
                    Log.w("ViewModel", "Users response body is null, using default ID")
                    currentUserId = 1
                }
            } else {
                Log.w("ViewModel", "Users HTTP error ${response.code()}: ${response.message()}, using default ID")
                currentUserId = 1
            }
        } catch (e: Exception) {
            Log.w("ViewModel", "User loading failed, using default ID: ${e.message}")
            currentUserId = 1
        }
    }

    private fun handleConnectionError(e: Exception, userMessage: String) {
        isConnectedToDatabase = false
        loadingState = LoadingState.ERROR
        errorMessage = "$userMessage\n${e.message}"
        showToast("❌ $userMessage")
        products = emptyList()
        categories = emptyList()
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
    // PAYMENT METHODS
    // =====================================================

    fun showPayment() {
        if (loadingState != LoadingState.SUCCESS) {
            showToast("⚠️ Please wait for data to load completely")
            return
        }

        if (cartItems.isEmpty()) {
            showToast("🛒 Your cart is empty")
            return
        }

        showPaymentDialog = true
        paymentState = PaymentState.SELECTING
    }

    fun hidePayment() {
        showPaymentDialog = false
        paymentState = PaymentState.NONE
    }

    // Replace the processPayment method in your Models.kt with this enhanced version

    fun processPayment(method: PaymentMethod) {
        // First validate allowed payment methods
        val allowedMethods = listOf(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.EDENRED,
            PaymentMethod.SODEXO
        )

        if (!allowedMethods.contains(method)) {
            paymentState = PaymentState.FAILED
            errorMessage = "Payment method not accepted"
            showToast("❌ ${method.displayName} is not accepted")
            return
        }

        if (loadingState != LoadingState.SUCCESS || products.isEmpty()) {
            showToast("❌ Cannot process payment: Data not loaded properly")
            paymentState = PaymentState.FAILED
            return
        }

        selectedPaymentMethod = method
        paymentState = PaymentState.PROCESSING

        scope.launch {
            try {
                Log.d("ViewModel", "🔄 Starting payment processing with method: ${method.displayName}")
                delay(2000) // Simulate processing time

                // STEP 1: Create order request
                val orderItems = cartItems.map { cartItem ->
                    CreateOrderItemRequest(
                        productId = cartItem.product.pid,
                        quantity = cartItem.quantity
                    )
                }

                val orderRequest = CreateOrderRequest(
                    userId = currentUserId,
                    deliveryAddress = "Default Delivery Address",
                    paymentMethod = method.apiValue,
                    orderItems = orderItems
                )

                Log.d("ViewModel", "📝 Order request: $orderRequest")

                val apiService = ApiClient.getApiService()
                val createOrderResponse = apiService.createOrder(orderRequest)

                Log.d("ViewModel", "📋 Order response code: ${createOrderResponse.code()}")
                Log.d("ViewModel", "📋 Order response successful: ${createOrderResponse.isSuccessful}")

                if (createOrderResponse.isSuccessful) {
                    createOrderResponse.body()?.let { response ->
                        Log.d("ViewModel", "📋 Order response body: $response")

                        if (response.success) {
                            // STEP 2: Update order status to "Confirmed" after successful payment
                            Log.d("ViewModel", "💳 Payment successful! Updating order status to Confirmed...")

                            val statusUpdateSuccess = updateOrderStatusToConfirmed(response.orderId)

                            if (statusUpdateSuccess) {
                                paymentState = PaymentState.SUCCESS
                                showToast("🎉 Payment successful! Order #${response.orderId} confirmed")
                                Log.i("ViewModel", "✅ Order ${response.orderId} created and confirmed successfully")

                                // Clear cart only after successful payment and confirmation
                                cartItems = emptyList()
                            } else {
                                // Payment succeeded but status update failed - offer option to cancel
                                paymentState = PaymentState.FAILED
                                errorMessage = "Payment processed but confirmation failed"
                                showToast("⚠️ Payment processed but confirmation failed. Order #${response.orderId}")

                                // Give user option to cancel the order
                                offerOrderCancellation(response.orderId)
                            }
                        } else {
                            paymentState = PaymentState.FAILED
                            errorMessage = response.message
                            showToast("❌ Order failed: ${response.message}")
                            Log.e("ViewModel", "❌ Order creation failed: ${response.message}")
                        }
                    } ?: run {
                        paymentState = PaymentState.FAILED
                        errorMessage = "Empty response from server"
                        showToast("❌ Payment failed: Empty response")
                        Log.e("ViewModel", "❌ Order response body is null")
                    }
                } else {
                    val errorBody = createOrderResponse.errorBody()?.string()
                    paymentState = PaymentState.FAILED
                    errorMessage = "HTTP ${createOrderResponse.code()}: ${createOrderResponse.message()}"
                    showToast("❌ Payment failed: HTTP ${createOrderResponse.code()}")
                    Log.e("ViewModel", "❌ Order HTTP error ${createOrderResponse.code()}: $errorBody")
                }
            } catch (e: Exception) {
                paymentState = PaymentState.FAILED
                errorMessage = "Payment failed: ${e.message}"
                showToast("❌ Payment processing failed: ${e.message}")
                Log.e("ViewModel", "❌ Payment processing exception", e)
            }
        }
    }

    // Add these new methods to your ViewModel
    private fun offerOrderCancellation(orderId: Int) {
        // This would typically trigger a UI dialog in your composable
        // For now we'll just log and show a toast
        Log.w("ViewModel", "⚠️ Offering cancellation for order $orderId")
        showToast("⚠️ Would you like to cancel order #$orderId?")

        // In a real app, you would:
        // 1. Show a dialog with cancel/retry options
        // 2. Call cancelOrder() if user chooses to cancel
    }

    private suspend fun cancelOrder(orderId: Int, reason: String = "Payment confirmation failed"): Boolean {
        return try {
            Log.d("ViewModel", "🔄 Attempting to cancel order $orderId...")

            val apiService = ApiClient.getApiService()
            val response = apiService.cancelOrder(
                CancelOrderRequest(
                    orderId = orderId,
                    userId = currentUserId,
                    reason = reason
                )
            )

            Log.d("ViewModel", "📊 Cancel response code: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { cancelResponse ->
                    Log.d("ViewModel", "📊 Cancel response: $cancelResponse")

                    if (cancelResponse.success) {
                        Log.i("ViewModel", "✅ Order $orderId cancelled successfully")
                        showToast("Order #$orderId cancelled")
                        true
                    } else {
                        Log.e("ViewModel", "❌ Cancel failed: ${cancelResponse.message}")
                        showToast("Failed to cancel order: ${cancelResponse.message}")
                        false
                    }
                } ?: run {
                    Log.e("ViewModel", "❌ Empty cancel response body")
                    showToast("Failed to cancel order: Empty response")
                    false
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ViewModel", "❌ Cancel HTTP error ${response.code()}: $errorBody")
                showToast("Failed to cancel order: HTTP ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "❌ Cancel order exception", e)
            showToast("Error cancelling order: ${e.message}")
            false
        }
    }

    // Add this new method to your ViewModel
    private suspend fun updateOrderStatusToConfirmed(orderId: Int): Boolean {
        return try {
            Log.d("ViewModel", "🔄 Updating order $orderId status to Confirmed...")

            val apiService = ApiClient.getApiService()
            val statusRequest = StatusUpdateRequest(newStatus = "Confirmed")
            val response = apiService.updateOrderStatus(orderId, statusRequest)

            Log.d("ViewModel", "📊 Status update response code: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { statusResponse ->
                    Log.d("ViewModel", "📊 Status update response: $statusResponse")

                    if (statusResponse.success) {
                        Log.i("ViewModel", "✅ Order $orderId status updated to Confirmed successfully")
                        true
                    } else {
                        Log.e("ViewModel", "❌ Status update failed: ${statusResponse.message}")
                        false
                    }
                } ?: run {
                    Log.e("ViewModel", "❌ Status update response body is null")
                    false
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ViewModel", "❌ Status update HTTP error ${response.code()}: $errorBody")
                false
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "❌ Status update exception for order $orderId", e)
            false
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

    private fun getEmojiForProduct(productName: String): String {
        return when {
            productName.contains("burger", ignoreCase = true) -> "🍔"
            productName.contains("cola", ignoreCase = true) -> "🥤"
            productName.contains("juice", ignoreCase = true) -> "🧃"
            productName.contains("water", ignoreCase = true) -> "💧"
            productName.contains("coffee", ignoreCase = true) -> "☕"
            productName.contains("fries", ignoreCase = true) -> "🍟"
            productName.contains("nuggets", ignoreCase = true) -> "🍗"
            productName.contains("cheese", ignoreCase = true) -> "🧀"
            else -> "🍽️"
        }
    }

    private fun showToast(message: String) {
        context?.let { Toast.makeText(it, message, Toast.LENGTH_SHORT).show() }
    }

    fun onCleared() {
        scope.cancel()
    }
}

typealias FoodOrderingViewModel = DemoFoodOrderingViewModel