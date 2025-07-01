
// domain/repositories/ConnectionRepository.kt
package com.example.foodorderingapp_ver2.domain.repositories

import com.example.foodorderingapp_ver2.domain.common.Result

interface ConnectionRepository {
    suspend fun testConnection(): Result<Boolean>
    fun isConnected(): Boolean
    suspend fun initialize(serverUrl: String): Result<Boolean>
}