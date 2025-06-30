
// domain/usecases/InitializeConnectionUseCase.kt
package com.example.foodorderingapp.domain.usecases

import com.example.foodorderingapp.domain.repositories.ConnectionRepository
import com.example.foodorderingapp.domain.common.Result
import com.example.foodorderingapp.domain.common.UseCase

class InitializeConnectionUseCase(
    private val connectionRepository: ConnectionRepository
) : UseCase<String, Boolean>() {

    override suspend fun invoke(parameters: String): Result<Boolean> {
        if (parameters.isBlank()) {
            return Result.Error("Server URL cannot be empty")
        }

        return connectionRepository.initialize(parameters)
    }
}