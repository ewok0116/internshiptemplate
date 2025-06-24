// network/ApiService.kt - COMPLETE VERSION
package com.example.foodorderingapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.annotations.SerializedName
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// =====================================================
// API DATA MODELS
// =====================================================

data class ApiProduct(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("categoryId") val categoryId: Int? = null,
    @SerializedName("isAvailable") val isAvailable: Boolean? = true
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

// Order creation models - Backend format
data class CreateOrderRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("deliveryAddress") val deliveryAddress: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("orderItems") val orderItems: List<CreateOrderItemRequest>
)

data class CreateOrderItemRequest(
    @SerializedName("productId") val productId: Int,
    @SerializedName("quantity") val quantity: Int
)

// Backend response
data class CreateOrderResponse(
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("orderStatus") val orderStatus: String,
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("orderDate") val orderDate: String,
    @SerializedName("deliveryAddress") val deliveryAddress: String,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("orderItems") val orderItems: List<CreatedOrderItemDto>,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class CreatedOrderItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("productId") val productId: Int,
    @SerializedName("productName") val productName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unitPrice") val unitPrice: Double,
    @SerializedName("totalPrice") val totalPrice: Double
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

    // FIXED ENDPOINT - Backend'inizle aynı
    @POST("api/orders")
    @Headers("Content-Type: application/json")
    suspend fun createOrder(@Body request: CreateOrderRequest): CreateOrderResponse

    @GET("api/orders")
    suspend fun getOrders(@Query("userId") userId: Int? = null): List<CreateOrderResponse>
}

// =====================================================
// API CLIENT
// =====================================================

object ApiClient {
    private var retrofit: Retrofit? = null
    private var apiService: FoodOrderingApiService? = null
    private var currentBaseUrl: String? = null

    fun initialize(baseUrl: String) {
        try {
            val formattedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

            if (currentBaseUrl == formattedUrl && apiService != null) {
                return
            }

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .serializeNulls()
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(formattedUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            apiService = retrofit?.create(FoodOrderingApiService::class.java)
            currentBaseUrl = formattedUrl

        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize API client: ${e.message}", e)
        }
    }

    fun getApiService(): FoodOrderingApiService {
        return apiService ?: throw IllegalStateException("ApiClient not initialized. Call initialize() first.")
    }

    fun isInitialized(): Boolean {
        return apiService != null
    }

    fun getCurrentBaseUrl(): String? {
        return currentBaseUrl
    }

    fun reset() {
        retrofit = null
        apiService = null
        currentBaseUrl = null
    }
}