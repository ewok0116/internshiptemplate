
// domain/usecases/TestConnectionUseCase.kt
package com.example.foodorderingapp_ver2.domain.usecases

import com.example.foodorderingapp_ver2.domain.repositories.ConnectionRepository
import com.example.foodorderingapp_ver2.domain.common.Result
import com.example.foodorderingapp_ver2.domain.common.NoParameterUseCase

class TestConnectionUseCase(
    private val connectionRepository: ConnectionRepository
) : NoParameterUseCase<Boolean>() {

    override suspend fun invoke(): Result<Boolean> {
        return connectionRepository.testConnection()
    }
}
