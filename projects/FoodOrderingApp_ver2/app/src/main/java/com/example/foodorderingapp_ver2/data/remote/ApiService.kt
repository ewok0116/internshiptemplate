// data/remote/ApiService.kt
package com.example.foodorderingapp_ver2.data.remote

import retrofit2.Response
import retrofit2.http.*
import com.google.gson.annotations.SerializedName

// Remote DTOs
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

data class ProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("categoryId") val categoryId: Int? = null,
    @SerializedName("isAvailable") val isAvailable: Boolean? = true
)

data class CategoryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String
)

data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)

data class CreateOrderRequestDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("deliveryAddress") val deliveryAddress: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("orderItems") val orderItems: List<CreateOrderItemRequestDto>
)

data class CreateOrderItemRequestDto(
    @SerializedName("productId") val productId: Int,
    @SerializedName("quantity") val quantity: Int
)

data class CreateOrderResponseDto(
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("orderStatus") val orderStatus: String,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("orderDate") val orderDate: String,
    @SerializedName("deliveryAddress") val deliveryAddress: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("orderItems") val orderItems: List<OrderItemDto>,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class OrderItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("productId") val productId: Int,
    @SerializedName("productName") val productName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("totalPrice") val totalPrice: Double
)

data class StatusUpdateRequestDto(
    @SerializedName("newStatus") val newStatus: String
)

data class UpdateOrderStatusResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("orderId") val orderId: Int? = null,
    @SerializedName("newStatus") val newStatus: String? = null
)

data class CancelOrderRequestDto(
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("reason") val reason: String? = null
)

data class CancelOrderResponseDto(
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("reason") val reason: String?,
    @SerializedName("cancelledAt") val cancelledAt: String,
    @SerializedName("previousStatus") val previousStatus: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class ConnectionTestResponseDto(
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String
)

interface FoodOrderingApiService {
    @GET("api/Database/test-connection")
    suspend fun testConnection(): Response<ConnectionTestResponseDto>

    @GET("api/products")
    suspend fun getProducts(): Response<ApiResponse<List<ProductDto>>>

    @GET("api/categories")
    suspend fun getCategories(): Response<ApiResponse<List<CategoryDto>>>

    @GET("api/users")
    suspend fun getUsers(): Response<ApiResponse<List<UserDto>>>

    @POST("api/create-order")
    @Headers("Content-Type: application/json")
    suspend fun createOrder(@Body request: CreateOrderRequestDto): Response<CreateOrderResponseDto>

    @PUT("api/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: Int,
        @Body request: StatusUpdateRequestDto
    ): Response<UpdateOrderStatusResponseDto>

    @POST("api/cancel-order")
    @Headers("Content-Type: application/json")
    suspend fun cancelOrder(@Body request: CancelOrderRequestDto): Response<CancelOrderResponseDto>
}