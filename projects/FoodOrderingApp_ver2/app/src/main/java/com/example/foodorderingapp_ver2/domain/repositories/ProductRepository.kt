
// domain/repositories/ProductRepository.kt
package com.example.foodorderingapp_ver2.domain.repositories

import com.example.foodorderingapp_ver2.domain.entities.Product
import com.example.foodorderingapp_ver2.domain.common.Result

interface ProductRepository {
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getProductById(id: Int): Result<Product>
    suspend fun getProductsByCategory(categoryId: Int): Result<List<Product>>
    suspend fun searchProducts(query: String): Result<List<Product>>
}