
// domain/repositories/ConnectionRepository.kt
package com.example.foodorderingapp.domain.repositories

import com.example.foodorderingapp.domain.common.Result

interface ConnectionRepository {
    suspend fun testConnection(): Result<Boolean>
    fun isConnected(): Boolean
    suspend fun initialize(serverUrl: String): Result<Boolean>
}