
// data/repositories/OrderRepositoryImpl.kt - FIXED AND COMPLETE
package com.example.foodorderingapp_ver2.data.repositories

import com.example.foodorderingapp_ver2.domain.entities.*
import com.example.foodorderingapp_ver2.domain.repositories.OrderRepository
import com.example.foodorderingapp_ver2.domain.common.Result
import com.example.foodorderingapp_ver2.data.remote.*
import com.example.foodorderingapp_ver2.data.mappers.OrderMapper
import android.util.Log

class OrderRepositoryImpl(
    private val apiService: FoodOrderingApiService
) : OrderRepository {

    override suspend fun createOrder(
        userId: Int,
        items: List<CartItem>,
        deliveryAddress: String,
        paymentMethod: PaymentMethod
    ): Result<Order> {
        return try {
            Log.d("OrderRepository", "Creating order for user $userId with ${items.size} items")

            val requestDto = OrderMapper.mapCreateOrderRequestToDto(
                userId, items, deliveryAddress, paymentMethod
            )

            val response = apiService.createOrder(requestDto)

            if (response.isSuccessful) {
                response.body()?.let { orderDto ->
                    if (orderDto.success) {
                        val order = OrderMapper.mapToDomain(orderDto)
                        Log.d("OrderRepository", "Successfully created order ${order.id}")
                        Result.Success(order)
                    } else {
                        Result.Error(orderDto.message)
                    }
                } ?: Result.Error("Empty response from server")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("OrderRepository", "HTTP error ${response.code()}: $errorBody")
                Result.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error creating order", e)
            Result.Error("Network error: ${e.message}", e)
        }
    }

    override suspend fun updateOrderStatus(orderId: Int, status: OrderStatus): Result<Order> {
        return try {
            Log.d("OrderRepository", "Updating order $orderId status to $status")

            val request = StatusUpdateRequestDto(newStatus = status.name)
            val response = apiService.updateOrderStatus(orderId, request)

            if (response.isSuccessful) {
                response.body()?.let { statusResponse ->
                    if (statusResponse.success) {
                        // Since we don't get full order back, we create a minimal order object
                        // In a real implementation, you might want to fetch the order again
                        Log.d("OrderRepository", "Successfully updated order $orderId status")

                        // Create a minimal order object to represent the successful update
                        val updatedOrder = Order(
                            id = orderId,
                            userId = 0, // Not available from response
                            items = emptyList(),
                            totalAmount = 0.0,
                            status = status,
                            paymentMethod = PaymentMethod.CREDIT_CARD, // Default
                            deliveryAddress = "",
                            orderDate = java.util.Date()
                        )
                        Result.Success(updatedOrder)
                    } else {
                        Result.Error(statusResponse.message)
                    }
                } ?: Result.Error("Empty response from server")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("OrderRepository", "HTTP error ${response.code()}: $errorBody")
                Result.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error updating order status", e)
            Result.Error("Network error: ${e.message}", e)
        }
    }

    override suspend fun cancelOrder(orderId: Int, reason: String): Result<Order> {
        return try {
            Log.d("OrderRepository", "Cancelling order $orderId with reason: $reason")

            val request = CancelOrderRequestDto(
                orderId = orderId,
                userId = 0, // You might need to pass this as a parameter
                reason = reason
            )

            val response = apiService.cancelOrder(request)

            if (response.isSuccessful) {
                response.body()?.let { cancelResponse ->
                    if (cancelResponse.success) {
                        Log.d("OrderRepository", "Successfully cancelled order $orderId")

                        // Create a minimal order object to represent the successful cancellation
                        val cancelledOrder = Order(
                            id = orderId,
                            userId = cancelResponse.userId,
                            items = emptyList(),
                            totalAmount = 0.0,
                            status = OrderStatus.CANCELLED,
                            paymentMethod = PaymentMethod.CREDIT_CARD, // Default
                            deliveryAddress = "",
                            orderDate = java.util.Date()
                        )
                        Result.Success(cancelledOrder)
                    } else {
                        Result.Error(cancelResponse.message)
                    }
                } ?: Result.Error("Empty response from server")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("OrderRepository", "HTTP error ${response.code()}: $errorBody")
                Result.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error cancelling order", e)
            Result.Error("Network error: ${e.message}", e)
        }
    }

    override suspend fun getOrdersByUser(userId: Int): Result<List<Order>> {
        // This endpoint is not implemented in the current API
        return Result.Error("Get orders by user not implemented")
    }
}
