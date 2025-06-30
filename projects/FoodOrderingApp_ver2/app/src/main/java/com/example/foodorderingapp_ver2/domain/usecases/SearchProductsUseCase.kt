
// domain/usecases/SearchProductsUseCase.kt
package com.example.foodorderingapp.domain.usecases

import com.example.foodorderingapp.domain.entities.Product
import com.example.foodorderingapp.domain.repositories.ProductRepository
import com.example.foodorderingapp.domain.common.Result
import com.example.foodorderingapp.domain.common.UseCase

class SearchProductsUseCase(
    private val productRepository: ProductRepository
) : UseCase<String, List<Product>>() {

    override suspend fun invoke(parameters: String): Result<List<Product>> {
        return if (parameters.isBlank()) {
            productRepository.getProducts()
        } else {
            productRepository.searchProducts(parameters)
        }
    }
}