
// domain/repositories/CategoryRepository.kt
package com.example.foodorderingapp_ver2.domain.repositories
import com.example.foodorderingapp_ver2.domain.entities.Category
import com.example.foodorderingapp_ver2.domain.common.Result

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getCategoryById(id: Int): Result<Category>
}