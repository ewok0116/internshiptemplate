package com.example.foodorderingapp_ver2.domain.entities

data class OrderItem(
    val id: Int,
    val productId: Int,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double
)