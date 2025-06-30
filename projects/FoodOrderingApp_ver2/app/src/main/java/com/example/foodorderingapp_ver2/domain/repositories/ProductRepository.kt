
// domain/repositories/ProductRepository.kt
package com.example.foodorderingapp.domain.repositories

import com.example.foodorderingapp.domain.entities.Product
import com.example.foodorderingapp.domain.common.Result

interface ProductRepository {
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getProductById(id: Int): Result<Product>
    suspend fun getProductsByCategory(categoryId: Int): Result<List<Product>>
    suspend fun searchProducts(query: String): Result<List<Product>>
}