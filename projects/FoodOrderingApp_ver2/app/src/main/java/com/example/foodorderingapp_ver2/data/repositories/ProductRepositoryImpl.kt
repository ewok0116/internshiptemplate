// data/repositories/ProductRepositoryImpl.kt
package com.example.foodorderingapp_ver2.data.repositories

import com.example.foodorderingapp_ver2.domain.entities.Product

import com.example.foodorderingapp_ver2.domain.repositories.ProductRepository
import com.example.foodorderingapp_ver2.domain.common.Result
import com.example.foodorderingapp_ver2.data.remote.FoodOrderingApiService
import com.example.foodorderingapp_ver2.data.mappers.ProductMapper
import android.util.Log

class ProductRepositoryImpl(
    private val apiService: FoodOrderingApiService
) : ProductRepository {

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            Log.d("ProductRepository", "Fetching products from API...")
            val response = apiService.getProducts()

            if (response.isSuccessful) {
                response.body()?.let { apiResponse ->
                    if (apiResponse.success) {
                        apiResponse.data?.let { productDtos ->
                            val products = ProductMapper.mapToDomainList(productDtos)
                            Log.d("ProductRepository", "Successfully fetched ${products.size} products")
                            Result.Success(products)
                        } ?: Result.Error("No products data in response")
                    } else {
                        Result.Error(apiResponse.message ?: "Failed to fetch products")
                    }
                } ?: Result.Error("Empty response from server")
            } else {
                Result.Error("HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error fetching products", e)
            Result.Error("Network error: ${e.message}", e)
        }
    }

    override suspend fun getProductById(id: Int): Result<Product> {
        return when (val result = getProducts()) {
            is Result.Success -> {
                val product = result.data.find { it.id == id }
                if (product != null) {
                    Result.Success(product)
                } else {
                    Result.Error("Product not found")
                }
            }
            is Result.Error -> result
            Result.Loading -> Result.Loading
        }
    }

    override suspend fun getProductsByCategory(categoryId: Int): Result<List<Product>> {
        return when (val result = getProducts()) {
            is Result.Success -> {
                val filteredProducts = result.data.filter { it.categoryId == categoryId }
                Result.Success(filteredProducts)
            }
            is Result.Error -> result
            Result.Loading -> Result.Loading
        }
    }

    override suspend fun searchProducts(query: String): Result<List<Product>> {
        return when (val result = getProducts()) {
            is Result.Success -> {
                val filteredProducts = result.data.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true)
                }
                Result.Success(filteredProducts)
            }
            is Result.Error -> result
            Result.Loading -> Result.Loading
        }
    }
}