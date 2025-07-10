
// domain/entities/Order.kt
package com.example.foodorderingapp_ver2.domain.entities

import java.util.Date

data class Order(
    val id: Int,
    val userId: Int,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod,
    val deliveryAddress: String,
    val orderDate: Date
)