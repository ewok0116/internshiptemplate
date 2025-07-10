// presentation/viewmodel/CartManager.kt
package com.example.foodorderingapp_ver2.presentation.viewmodel

import android.util.Log
import com.example.foodorderingapp_ver2.domain.entities.Product

class CartManager(
    private val onShowToast: (String) -> Unit,
    private val onStateUpdate: (FoodOrderingUiState) -> Unit,
    private val getCurrentState: () -> FoodOrderingUiState
) {

    // =====================================================
    // CART METHODS
    // =====================================================

    fun addToCart(product: Product) {
        val currentState = getCurrentState()
        val existingItem = currentState.cartItems.find { it.product.id == product.id }

        val updatedCartItems = if (existingItem != null) {
            currentState.cartItems.map {
                if (it.product.id == product.id) {
                    it.copy(quantity = it.quantity + 1)
                } else it
            }
        } else {
            currentState.cartItems + CartItemUi(product, 1)
        }

        onStateUpdate(currentState.copy(cartItems = updatedCartItems))
        onShowToast("➕ ${product.name} added to cart")

        Log.d("CartManager", "Added ${product.name} to cart. Total items: ${updatedCartItems.size}")
    }

    fun removeFromCart(productId: Int) {
        val currentState = getCurrentState()
        val removedItem = currentState.cartItems.find { it.product.id == productId }
        val updatedCartItems = currentState.cartItems.filter { it.product.id != productId }

        onStateUpdate(currentState.copy(cartItems = updatedCartItems))

        removedItem?.let {
            onShowToast("🗑️ ${it.product.name} removed from cart")
            Log.d("CartManager", "Removed ${it.product.name} from cart. Remaining items: ${updatedCartItems.size}")
        }
    }

    fun updateQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
        } else {
            val currentState = getCurrentState()
            val updatedCartItems = currentState.cartItems.map {
                if (it.product.id == productId) {
                    it.copy(quantity = quantity)
                } else it
            }

            onStateUpdate(currentState.copy(cartItems = updatedCartItems))

            val updatedItem = updatedCartItems.find { it.product.id == productId }
            updatedItem?.let {
                Log.d("CartManager", "Updated ${it.product.name} quantity to $quantity")
            }
        }
    }

    // =====================================================
    // CART UI METHODS
    // =====================================================

    fun showCart() {
        val currentState = getCurrentState()
        onStateUpdate(currentState.copy(showCartDialog = true))
        Log.d("CartManager", "Showing cart dialog with ${currentState.cartItems.size} items")
    }

    fun hideCart() {
        val currentState = getCurrentState()
        onStateUpdate(currentState.copy(showCartDialog = false))
        Log.d("CartManager", "Hiding cart dialog")
    }

    // =====================================================
    // CART UTILITY METHODS
    // =====================================================

    fun clearCart() {
        val currentState = getCurrentState()
        val itemCount = currentState.cartItems.size

        onStateUpdate(currentState.copy(cartItems = emptyList()))
        onShowToast("🧹 Cart cleared")

        Log.d("CartManager", "Cleared cart - removed $itemCount items")
    }

    fun clearCartAndHidePayment() {
        val currentState = getCurrentState()

        Log.d("CartManager", "🧹 User chose to clear cart and go home")

        onStateUpdate(currentState.copy(
            cartItems = emptyList(),
            completedOrderItems = emptyList(), // Clear receipt data too
            showPaymentDialog = false,
            paymentState = PaymentStateUi.NONE,
            lastOrderId = null
        ))

        Log.d("CartManager", "🏠 Returning to home - all order data cleared")
        onShowToast("🏠 Returning to home")
    }

    fun getCartItemsSummary(): String {
        val currentState = getCurrentState()
        return currentState.receiptItems.joinToString(", ") { "${it.product.name} x${it.quantity}" }
    }

    // =====================================================
    // CART VALIDATION METHODS
    // =====================================================

    fun isCartEmpty(): Boolean {
        return getCurrentState().cartItems.isEmpty()
    }

    fun getCartItemCount(): Int {
        return getCurrentState().cartItems.sumOf { it.quantity }
    }

    fun getCartTotal(): Double {
        return getCurrentState().cartItems.sumOf { it.subtotal }
    }

    fun hasProductInCart(productId: Int): Boolean {
        return getCurrentState().cartItems.any { it.product.id == productId }
    }

    fun getProductQuantityInCart(productId: Int): Int {
        return getCurrentState().cartItems.find { it.product.id == productId }?.quantity ?: 0
    }

    // =====================================================
    // CART INFO METHODS
    // =====================================================

    fun getCartStatus(): String {
        val currentState = getCurrentState()
        return when {
            currentState.cartItems.isEmpty() -> "Cart is empty"
            currentState.cartItems.size == 1 -> "1 item in cart"
            else -> "${currentState.cartItems.size} items in cart"
        }
    }

    fun getCartDetails(): String {
        val currentState = getCurrentState()
        val itemCount = getCartItemCount()
        val total = getCartTotal()

        return "Cart: $itemCount items, Total: ${total} TL"
    }

    // =====================================================
    // CART OPERATIONS FOR SPECIFIC SCENARIOS
    // =====================================================

    fun increaseQuantity(productId: Int) {
        val currentQuantity = getProductQuantityInCart(productId)
        if (currentQuantity > 0) {
            updateQuantity(productId, currentQuantity + 1)
        }
    }

    fun decreaseQuantity(productId: Int) {
        val currentQuantity = getProductQuantityInCart(productId)
        if (currentQuantity > 1) {
            updateQuantity(productId, currentQuantity - 1)
        } else if (currentQuantity == 1) {
            removeFromCart(productId)
        }
    }

    fun duplicateCartItem(productId: Int) {
        val currentState = getCurrentState()
        val existingItem = currentState.cartItems.find { it.product.id == productId }

        existingItem?.let { item ->
            addToCart(item.product)
            onShowToast("📋 ${item.product.name} duplicated in cart")
        }
    }

    // =====================================================
    // CART BULK OPERATIONS
    // =====================================================

    fun addMultipleToCart(products: List<Product>) {
        products.forEach { product ->
            addToCart(product)
        }
        onShowToast("➕ Added ${products.size} products to cart")
    }

    fun removeMultipleFromCart(productIds: List<Int>) {
        productIds.forEach { productId ->
            removeFromCart(productId)
        }
        onShowToast("🗑️ Removed ${productIds.size} products from cart")
    }

    // Save current cart state for recovery
    fun saveCartState(): List<CartItemUi> {
        return getCurrentState().cartItems.toList()
    }

    // Restore cart from saved state
    fun restoreCartState(savedCartItems: List<CartItemUi>) {
        val currentState = getCurrentState()
        onStateUpdate(currentState.copy(cartItems = savedCartItems))
        onShowToast("🔄 Cart restored")
        Log.d("CartManager", "Restored cart with ${savedCartItems.size} items")
    }
}