
// domain/usecases/GetProductsByCategoryUseCase.kt
package com.example.foodorderingapp.domain.usecases

import com.example.foodorderingapp.domain.entities.Product
import com.example.foodorderingapp.domain.repositories.ProductRepository
import com.example.foodorderingapp.domain.common.Result
import com.example.foodorderingapp.domain.common.UseCase

class GetProductsByCategoryUseCase(
    private val productRepository: ProductRepository
) : UseCase<Int, List<Product>>() {

    override suspend fun invoke(parameters: Int): Result<List<Product>> {
        return productRepository.getProductsByCategory(parameters)
    }
}