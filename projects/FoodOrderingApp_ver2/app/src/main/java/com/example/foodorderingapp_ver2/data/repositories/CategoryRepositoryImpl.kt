
// data/repositories/CategoryRepositoryImpl.kt - FIXED
package com.example.foodorderingapp_ver2.data.repositories
import com.example.foodorderingapp_ver2.domain.entities.Category
import com.example.foodorderingapp_ver2.domain.repositories.CategoryRepository
import com.example.foodorderingapp_ver2.domain.common.Result
import com.example.foodorderingapp_ver2.data.remote.FoodOrderingApiService
import com.example.foodorderingapp_ver2.data.mappers.CategoryMapper
import android.util.Log

class CategoryRepositoryImpl(
    private val apiService: FoodOrderingApiService
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            Log.d("CategoryRepository", "Fetching categories from API...")
            val response = apiService.getCategories()

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data?.let { categoryDtos ->
                            val categories = CategoryMapper.mapToDomainList(categoryDtos)
                            Log.d("CategoryRepository", "Successfully fetched ${categories.size} categories")
                            Result.Success(categories)
                        } ?: Result.Success(emptyList()) // Categories are optional
                    } else {
                        Log.w("CategoryRepository", "API returned success=false: ${apiResponse.message}")
                        Result.Success(emptyList()) // Categories are optional
                    }
                } ?: Result.Error("Empty response from server")
            } else {
                Log.e("CategoryRepository", "HTTP error ${response.code()}: ${response.message()}")
                Result.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("CategoryRepository", "Error fetching categories", e)
            Result.Error("Network error: ${e.message}", e)
        }
    }

    override suspend fun getCategoryById(id: Int): Result<Category> {
        return when (val result = getCategories()) {
            is Result.Success -> {
                val category = result.data.find { it.id == id }
                if (category != null) {
                    Result.Success(category)
                } else {
                    Result.Error("Category not found")
                }
            }
            is Result.Error -> result
            Result.Loading -> Result.Loading
        }
    }
}