// data/repositories/UserRepositoryImpl.kt - FIXED
package com.example.foodorderingapp_ver2.data.repositories

import com.example.foodorderingapp_ver2.domain.entities.User
import com.example.foodorderingapp_ver2.domain.repositories.UserRepository
import com.example.foodorderingapp_ver2.domain.common.Result
import com.example.foodorderingapp_ver2.data.remote.FoodOrderingApiService
import com.example.foodorderingapp_ver2.data.mappers.UserMapper
import android.util.Log

class UserRepositoryImpl(
    private val apiService: FoodOrderingApiService
) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> {
        return try {
            Log.d("UserRepository", "Fetching users from API...")
            val response = apiService.getUsers()

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data?.let { userDtos ->
                            val users = UserMapper.mapToDomainList(userDtos)
                            Log.d("UserRepository", "Successfully fetched ${users.size} users")
                            Result.Success(users)
                        } ?: Result.Success(emptyList())
                    } else {
                        Log.w("UserRepository", "API returned success=false: ${apiResponse.message}")
                        Result.Success(emptyList())
                    }
                } ?: Result.Error("Empty response from server")
            } else {
                Log.e("UserRepository", "HTTP error ${response.code()}: ${response.message()}")
                Result.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching users", e)
            Result.Error("Network error: ${e.message}", e)
        }
    }

    override suspend fun getUserById(id: Int): Result<User> {
        return when (val result = getUsers()) {
            is Result.Success -> {
                val user = result.data.find { it.id == id }
                if (user != null) {
                    Result.Success(user)
                } else {
                    Result.Error("User not found")
                }
            }
            is Result.Error -> result
            Result.Loading -> Result.Loading
        }
    }
}
