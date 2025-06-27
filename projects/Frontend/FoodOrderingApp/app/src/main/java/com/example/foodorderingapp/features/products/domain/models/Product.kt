// features/products/domain/models/Product.kt
package com.example.foodorderingapp.features.products.domain.models

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String = "",
    val categoryId: Int = 0,
    val imageUrl: String = "",
    val isAvailable: Boolean = true
) {
    val formattedPrice: String
        get() = "₺${String.format("%.2f", price)}"

    fun getEmojiIcon(): String {
        return when {
            name.contains("burger", ignoreCase = true) -> "🍔"
            name.contains("cola", ignoreCase = true) -> "🥤"
            name.contains("juice", ignoreCase = true) -> "🧃"
            name.contains("water", ignoreCase = true) -> "💧"
            name.contains("coffee", ignoreCase = true) -> "☕"
            name.contains("fries", ignoreCase = true) -> "🍟"
            name.contains("nuggets", ignoreCase = true) -> "🍗"
            name.contains("cheese", ignoreCase = true) -> "🧀"
            else -> "🍽️"
        }
    }
}
