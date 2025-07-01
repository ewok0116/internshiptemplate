
// domain/repositories/UserRepository.kt
package com.example.foodorderingapp_ver2.domain.repositories

import com.example.foodorderingapp_ver2.domain.entities.User
import com.example.foodorderingapp_ver2.domain.common.Result

interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserById(id: Int): Result<User>
}