// domain/entities/Product.kt
package com.example.foodorderingapp_ver2.domain.entities

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val categoryId: Int,
    val imageUrl: String,
    val isAvailable: Boolean = true
)