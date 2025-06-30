
// data/repositories/ConnectionRepositoryImpl.kt - FIXED
package com.example.foodorderingapp.data.repositories

import com.example.foodorderingapp.domain.repositories.ConnectionRepository
import com.example.foodorderingapp.domain.common.Result
import com.example.foodorderingapp.data.remote.ApiClient
import android.util.Log

class ConnectionRepositoryImpl : ConnectionRepository {

    override suspend fun testConnection(): Result<Boolean> {
        return try {
            if (!ApiClient.isInitialized()) {
                return Result.Error("API client not initialized")
            }

            Log.d("ConnectionRepository", "Testing connection...")
            val apiService = ApiClient.getApiService()
            val response = apiService.testConnection()

            if (response.isSuccessful) {
                response.body()?.let {
                    Log.d("ConnectionRepository", "Connection test successful")
                    Result.Success(true)
                } ?: Result.Error("Empty response from server")
            } else {
                Log.e("ConnectionRepository", "Connection test failed: ${response.code()}")
                Result.Error("Connection test failed: HTTP ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("ConnectionRepository", "Connection test error", e)
            when (e) {
                is java.net.ConnectException -> Result.Error("Cannot connect to server")
                is java.net.UnknownHostException -> Result.Error("Invalid server URL")
                is java.net.SocketTimeoutException -> Result.Error("Connection timeout")
                else -> Result.Error("Connection error: ${e.message}")
            }
        }
    }

    override fun isConnected(): Boolean {
        return ApiClient.isInitialized()
    }

    override suspend fun initialize(serverUrl: String): Result<Boolean> {
        return try {
            Log.d("ConnectionRepository", "Initializing connection to: $serverUrl")
            ApiClient.initialize(serverUrl)
            testConnection()
        } catch (e: Exception) {
            Log.e("ConnectionRepository", "Failed to initialize connection", e)
            Result.Error("Failed to initialize: ${e.message}")
        }
    }
}
