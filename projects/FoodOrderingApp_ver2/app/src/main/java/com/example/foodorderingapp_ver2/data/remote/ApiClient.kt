
// data/remote/ApiClient.kt
package com.example.foodorderingapp_ver2.data.remote
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

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
