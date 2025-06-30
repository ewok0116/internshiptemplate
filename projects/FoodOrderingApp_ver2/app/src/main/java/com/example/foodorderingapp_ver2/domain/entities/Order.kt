
// domain/entities/Order.kt
package com.example.foodorderingapp.domain.entities

import java.util.Date

data class Order(
    val id: Int,
    val userId: Int,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: OrderStatus,
    val paymentMethod: PaymentMethod,
    val deliveryAddress: String,
    val orderDate: Date
)