
// domain/repositories/UserRepository.kt
package com.example.foodorderingapp.domain.repositories

import com.example.foodorderingapp.domain.entities.User
import com.example.foodorderingapp.domain.common.Result

interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserById(id: Int): Result<User>
}