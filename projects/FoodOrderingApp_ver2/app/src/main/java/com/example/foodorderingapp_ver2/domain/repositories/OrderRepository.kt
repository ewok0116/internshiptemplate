
// domain/repositories/OrderRepository.kt
package com.example.foodorderingapp_ver2.domain.repositories

import com.example.foodorderingapp_ver2.domain.entities.*
import com.example.foodorderingapp_ver2.domain.common.Result

interface OrderRepository {
    suspend fun createOrder(
        userId: Int,
        items: List<CartItem>,
        deliveryAddress: String,
        paymentMethod: PaymentMethod
    ): Result<Order>

    suspend fun updateOrderStatus(orderId: Int, status: OrderStatus): Result<Order>
    suspend fun cancelOrder(orderId: Int, reason: String): Result<Order>
    suspend fun getOrdersByUser(userId: Int): Result<List<Order>>
}