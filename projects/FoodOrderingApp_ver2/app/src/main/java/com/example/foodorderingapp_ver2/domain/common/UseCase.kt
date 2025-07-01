
// domain/common/UseCase.kt
package com.example.foodorderingapp_ver2.domain.common

abstract class UseCase<in P, R> {
    abstract suspend operator fun invoke(parameters: P): Result<R>
}

abstract class NoParameterUseCase<R> {
    abstract suspend operator fun invoke(): Result<R>
}