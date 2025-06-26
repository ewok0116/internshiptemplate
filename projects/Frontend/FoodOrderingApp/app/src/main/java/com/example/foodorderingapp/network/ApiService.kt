// network/ApiService.kt - FIXED with correct endpoint
package com.example.foodorderingapp.network
// Add this import at the top of your Models.kt file
import com.example.foodorderingapp.network.StatusUpdateRequest
import retrofit2.Response
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

// Wrapper for API responses
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

data class CancelOrderRequest(
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("reason") val reason: String? = null
)

// Cancel Reason Request (for alternative endpoint)
data class CancelReasonRequest(
    @SerializedName("reason") val reason: String?
)

// Cancel Order Response
data class CancelOrderResponse(
    @SerializedName("orderId") val orderId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("reason") val reason: String?,
    @SerializedName("cancelledAt") val cancelledAt: String,
    @SerializedName("previousStatus") val previousStatus: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)


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

data class StatusUpdateRequest(
    @SerializedName("newStatus") val newStatus: String
)

data class UpdateOrderStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("orderId") val orderId: Int? = null,
    @SerializedName("newStatus") val newStatus: String? = null
)

// =====================================================
// RETROFIT API INTERFACE - FIXED ENDPOINT
// =====================================================

interface FoodOrderingApiService {
    @GET("api/Database/test-connection")
    suspend fun testConnection(): Response<ConnectionTestResponse>

    // Wrapper responses with success/data structure
    @GET("api/products")
    suspend fun getProducts(): Response<ApiResponse<List<ApiProduct>>>

    @GET("api/categories")
    suspend fun getCategories(): Response<ApiResponse<List<ApiCategory>>>

    @GET("api/users")
    suspend fun getUsers(): Response<ApiResponse<List<ApiUser>>>

    // FIXED: Correct order endpoint
    @POST("api/create-order")
    @Headers("Content-Type: application/json")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<CreateOrderResponse>

    @GET("api/orders")
    suspend fun getOrders(@Query("userId") userId: Int? = null): Response<List<CreateOrderResponse>>

    @PUT("api/orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: Int,
        @Body request: StatusUpdateRequest
    ): Response<UpdateOrderStatusResponse>


    /**
     * Cancel an order with full request body
     */
    @POST("api/cancel-order")
    @Headers("Content-Type: application/json")
    suspend fun cancelOrder(@Body request: CancelOrderRequest): Response<CancelOrderResponse>

    /**
     * Cancel order using path parameters (alternative endpoint)
     */
    @POST("api/cancel-order/{orderId}/user/{userId}")
    @Headers("Content-Type: application/json")
    suspend fun cancelOrderByIds(
        @Path("orderId") orderId: Int,
        @Path("userId") userId: Int,
        @Body request: CancelReasonRequest? = null
    ): Response<CancelOrderResponse>

    /**
     * Health check for cancel order service
     */
    @GET("api/cancel-order/health")
    suspend fun checkCancelOrderHealth(): Response<ConnectionTestResponse>
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