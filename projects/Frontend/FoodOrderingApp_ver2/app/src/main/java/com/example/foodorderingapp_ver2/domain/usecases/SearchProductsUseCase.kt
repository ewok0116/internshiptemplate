
// domain/usecases/SearchProductsUseCase.kt
package com.example.foodorderingapp_ver2.domain.usecases

import com.example.foodorderingapp_ver2.domain.entities.Product
import com.example.foodorderingapp_ver2.domain.repositories.ProductRepository
import com.example.foodorderingapp_ver2.domain.common.Result
import com.example.foodorderingapp_ver2.domain.common.UseCase

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