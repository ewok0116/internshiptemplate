
// features/products/domain/repository/ProductRepository.kt
package com.example.foodorderingapp.features.products.domain.repository

import com.example.foodorderingapp.core.network.NetworkResult
import com.example.foodorderingapp.features.products.domain.models.Product
import com.example.foodorderingapp.features.products.domain.models.Category

interface ProductRepository {
    suspend fun getProducts(): NetworkResult<List<Product>>
    suspend fun getCategories(): NetworkResult<List<Category>>
    suspend fun getProductsByCategory(categoryId: Int): NetworkResult<List<Product>>
    suspend fun searchProducts(query: String): List<Product>
}
