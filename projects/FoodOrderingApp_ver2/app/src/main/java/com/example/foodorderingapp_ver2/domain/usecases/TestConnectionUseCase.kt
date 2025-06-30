
// domain/usecases/TestConnectionUseCase.kt
package com.example.foodorderingapp.domain.usecases

import com.example.foodorderingapp.domain.repositories.ConnectionRepository
import com.example.foodorderingapp.domain.common.Result
import com.example.foodorderingapp.domain.common.NoParameterUseCase

class TestConnectionUseCase(
    private val connectionRepository: ConnectionRepository
) : NoParameterUseCase<Boolean>() {

    override suspend fun invoke(): Result<Boolean> {
        return connectionRepository.testConnection()
    }
}
