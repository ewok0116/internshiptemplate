// Replace your existing models/Models.kt file with this (NO MOCK DATA):
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
                showToast("🔄 Connecting to database...")

                // Test connection first
                val apiService = ApiClient.getApiService()
                val connectionResult = apiService.testConnection()
                isConnectedToDatabase = true
                showToast("✅ Connected: ${connectionResult.message}")
                Log.i("ViewModel", "Database connection successful: ${connectionResult.message}")

                // Load products from API
                val apiProducts = apiService.getProducts()
                products = apiProducts.map { apiProduct ->
                    Product(
                        pid = apiProduct.id,
                        pname = apiProduct.name,
                        price = apiProduct.price,
                        description = apiProduct.description,
                        imageUrl = getEmojiForProduct(apiProduct.name)
                    )
                }

                // Load categories from API
                val apiCategories = apiService.getCategories()
                categories = apiCategories.map { apiCategory ->
                    Category(
                        cid = apiCategory.id,
                        cname = apiCategory.name,
                        description = apiCategory.description
                    )
                }

                // Load users (to get current user ID)
                try {
                    val apiUsers = apiService.getUsers()
                    if (apiUsers.isNotEmpty()) {
                        currentUserId = apiUsers.first().id
                        Log.i("ViewModel", "Using user ID: $currentUserId")
                    }
                } catch (e: Exception) {
                    Log.w("ViewModel", "Could not load users: ${e.message}")
                }

                if (products.isNotEmpty()) {
                    loadingState = LoadingState.SUCCESS
                    showToast("📦 Loaded ${products.size} products from database")
                    Log.i("ViewModel", "Successfully loaded ${products.size} products and ${categories.size} categories")
                } else {
                    loadingState = LoadingState.ERROR
                    errorMessage = "No products found in database"
                    showToast("⚠️ No products found in database")
                }

            } catch (e: Exception) {
                isConnectedToDatabase = false
                loadingState = LoadingState.ERROR
                errorMessage = "Connection failed: ${e.message}"
                showToast("❌ Connection failed: ${e.message}")
                Log.e("ViewModel", "Failed to connect to database", e)

                // Don't use mock data - keep lists empty
                products = emptyList()
                categories = emptyList()
            }
        }
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
    // PAYMENT METHODS
    // =====================================================

    fun showPayment() {
        if (!isConnectedToDatabase) {
            showToast("⚠️ Not connected to database. Please check your connection.")
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

    fun processPayment(method: PaymentMethod) {
        if (!isConnectedToDatabase) {
            showToast("❌ Cannot process payment: Not connected to database")
            paymentState = PaymentState.FAILED
            return
        }

        selectedPaymentMethod = method
        paymentState = PaymentState.PROCESSING

        scope.launch {
            try {
                delay(2000) // Simulate processing time

                // Create real order in database
                val orderItems = cartItems.map { cartItem ->
                    CreateOrderItemRequest(
                        productId = cartItem.product.pid,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.product.price,
                        totalPrice = cartItem.subtotal
                    )
                }

                val orderRequest = CreateOrderRequest(
                    userId = currentUserId,
                    totalAmount = cartTotal,
                    deliveryAddress = "Default Delivery Address", // You can make this configurable
                    paymentMethod = method.apiValue,
                    orderItems = orderItems
                )

                val apiService = ApiClient.getApiService()
                val response = apiService.createOrder(orderRequest)

                paymentState = PaymentState.SUCCESS
                showToast("🎉 Order created! Order ID: ${response.orderId}")
                Log.i("ViewModel", "Order created successfully: ${response.message}")

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