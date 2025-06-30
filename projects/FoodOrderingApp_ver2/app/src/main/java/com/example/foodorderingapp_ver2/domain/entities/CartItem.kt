
// domain/entities/CartItem.kt
package com.example.foodorderingapp.domain.entities

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal: Double get() = product.price * quantity
}