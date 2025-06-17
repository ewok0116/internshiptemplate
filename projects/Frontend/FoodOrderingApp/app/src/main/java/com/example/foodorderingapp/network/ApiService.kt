// Create this file: app/src/main/java/com/example/foodorderingapp/network/ApiService.kt
package com.example.foodorderingapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.annotations.SerializedName

// =====================================================
// API DATA MODELS (matching your C# API responses)
// =====================================================

data class ApiProduct(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double
)

data class ApiCategory(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String
)

data class ApiUser(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String
)

// Order creation models
data class CreateOrderRequest(
    @SerializedName("UserId") val userId: Int,
    @SerializedName("TotalAmount") val totalAmount: Double,
    @SerializedName("DeliveryAddress") val deliveryAddress: String,
    @SerializedName("PaymentMethod") val paymentMethod: String,
    @SerializedName("OrderItems") val orderItems: List<CreateOrderItemRequest>
)

data class CreateOrderItemRequest(
    @SerializedName("ProductId") val productId: Int,
    @SerializedName("Quantity") val quantity: Int,
    @SerializedName("UnitPrice") val unitPrice: Double,
    @SerializedName("TotalPrice") val totalPrice: Double
)

data class CreateOrderResponse(
    @SerializedName("OrderId") val orderId: Int,
    @SerializedName("Message") val message: String,
    @SerializedName("OrderDate") val orderDate: String
)

data class ConnectionTestResponse(
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String
)

// =====================================================
// RETROFIT API INTERFACE
// =====================================================

interface FoodOrderingApiService {

    @GET("api/Database/test-connection")
    suspend fun testConnection(): ConnectionTestResponse

    @GET("api/Database/products")
    suspend fun getProducts(): List<ApiProduct>

    @GET("api/Database/categories")
    suspend fun getCategories(): List<ApiCategory>

    @GET("api/Database/users")
    suspend fun getUsers(): List<ApiUser>

    @POST("api/Database/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): CreateOrderResponse
}

// =====================================================
// API CLIENT SINGLETON
// =====================================================

object ApiClient {
    private var retrofit: Retrofit? = null
    private var apiService: FoodOrderingApiService? = null

    fun initialize(baseUrl: String) {
        // Ensure baseUrl ends with /
        val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        retrofit = Retrofit.Builder()
            .baseUrl(formattedUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit?.create(FoodOrderingApiService::class.java)
    }

    fun getApiService(): FoodOrderingApiService {
        return apiService ?: throw IllegalStateException("ApiClient not initialized. Call initialize() first.")
    }

    fun isInitialized(): Boolean {
        return apiService != null
    }

    fun reset() {
        retrofit = null
        apiService = null
    }
}