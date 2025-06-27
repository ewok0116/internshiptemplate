
// features/products/domain/models/Category.kt
package com.example.foodorderingapp.features.products.domain.models

data class Category(
    val id: Int,
    val name: String,
    val description: String = ""
)
