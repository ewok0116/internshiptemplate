
// domain/usecases/GetProductsUseCase.kt
package com.example.foodorderingapp.domain.usecases

import com.example.foodorderingapp.domain.entities.Product
import com.example.foodorderingapp.domain.repositories.ProductRepository
import com.example.foodorderingapp.domain.common.Result
import com.example.foodorderingapp.domain.common.NoParameterUseCase

class GetProductsUseCase(
    private val productRepository: ProductRepository
) : NoParameterUseCase<List<Product>>() {

    override suspend fun invoke(): Result<List<Product>> {
        return productRepository.getProducts()
    }
}
