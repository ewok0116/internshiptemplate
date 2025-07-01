
// domain/usecases/GetProductsUseCase.kt
package com.example.foodorderingapp_ver2.domain.usecases

import com.example.foodorderingapp_ver2.domain.entities.Product
import com.example.foodorderingapp_ver2.domain.repositories.ProductRepository
import com.example.foodorderingapp_ver2.domain.common.Result
import com.example.foodorderingapp_ver2.domain.common.NoParameterUseCase

class GetProductsUseCase(
    private val productRepository: ProductRepository
) : NoParameterUseCase<List<Product>>() {

    override suspend fun invoke(): Result<List<Product>> {
        return productRepository.getProducts()
    }
}
