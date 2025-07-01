
// domain/usecases/GetProductsByCategoryUseCase.kt
package com.example.foodorderingapp_ver2.domain.usecases

import com.example.foodorderingapp_ver2.domain.entities.Product
import com.example.foodorderingapp_ver2.domain.repositories.ProductRepository
import com.example.foodorderingapp_ver2.domain.common.Result
import com.example.foodorderingapp_ver2.domain.common.UseCase

class GetProductsByCategoryUseCase(
    private val productRepository: ProductRepository
) : UseCase<Int, List<Product>>() {

    override suspend fun invoke(parameters: Int): Result<List<Product>> {
        return productRepository.getProductsByCategory(parameters)
    }
}