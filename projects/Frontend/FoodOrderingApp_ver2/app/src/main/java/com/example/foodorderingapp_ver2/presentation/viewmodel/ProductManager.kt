// presentation/viewmodel/ProductManager.kt
package com.example.foodorderingapp_ver2.presentation.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.util.Log
import com.example.foodorderingapp_ver2.domain.entities.Product
import com.example.foodorderingapp_ver2.domain.usecases.SearchProductsUseCase
import com.example.foodorderingapp_ver2.domain.usecases.GetProductsByCategoryUseCase
import com.example.foodorderingapp_ver2.domain.common.Result

class ProductManager(
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val coroutineScope: CoroutineScope,
    private val getCurrentState: () -> FoodOrderingUiState
) {

    // =====================================================
    // PRODUCT SEARCH METHODS
    // =====================================================

    fun searchProducts(query: String, callback: (List<Product>) -> Unit) {
        Log.d("ProductManager", "Searching products with query: '$query'")

        coroutineScope.launch {
            when (val result = searchProductsUseCase(query)) {
                is Result.Success -> {
                    Log.d("ProductManager", "Search successful: found ${result.data.size} products")
                    callback(result.data)
                }
                is Result.Error -> {
                    Log.e("ProductManager", "Search failed: ${result.message}")
                    callback(emptyList())
                }
                Result.Loading -> {
                    Log.d("ProductManager", "Search loading...")
                }
            }
        }
    }

    // =====================================================
    // CATEGORY METHODS
    // =====================================================

    fun getProductsByCategory(categoryId: Int, callback: (List<Product>) -> Unit) {
        Log.d("ProductManager", "Getting products for category ID: $categoryId")

        coroutineScope.launch {
            when (val result = getProductsByCategoryUseCase(categoryId)) {
                is Result.Success -> {
                    Log.d("ProductManager", "Category filter successful: found ${result.data.size} products")
                    callback(result.data)
                }
                is Result.Error -> {
                    Log.e("ProductManager", "Category filter failed: ${result.message}")
                    callback(emptyList())
                }
                Result.Loading -> {
                    Log.d("ProductManager", "Category filtering loading...")
                }
            }
        }
    }

    fun getCategoryName(categoryId: Int): String {
        val currentState = getCurrentState()
        val categoryName = currentState.categories.find { it.id == categoryId }?.name ?: "All Products"

        Log.d("ProductManager", "Category ID $categoryId resolved to: '$categoryName'")
        return categoryName
    }

    // =====================================================
    // PRODUCT UTILITY METHODS
    // =====================================================

    fun getAllProducts(): List<Product> {
        return getCurrentState().products
    }

    fun getAllCategories(): List<com.example.foodorderingapp_ver2.domain.entities.Category> {
        return getCurrentState().categories
    }

    fun getProductById(productId: Int): Product? {
        val product = getCurrentState().products.find { it.id == productId }
        Log.d("ProductManager", "Product ID $productId ${if (product != null) "found" else "not found"}")
        return product
    }

    fun getCategoryById(categoryId: Int): com.example.foodorderingapp_ver2.domain.entities.Category? {
        val category = getCurrentState().categories.find { it.id == categoryId }
        Log.d("ProductManager", "Category ID $categoryId ${if (category != null) "found" else "not found"}")
        return category
    }

    // =====================================================
    // PRODUCT FILTERING METHODS
    // =====================================================

    fun filterProductsByName(searchTerm: String): List<Product> {
        val currentState = getCurrentState()
        val filteredProducts = currentState.products.filter {
            it.name.contains(searchTerm, ignoreCase = true)
        }

        Log.d("ProductManager", "Local filter by name '$searchTerm': found ${filteredProducts.size} products")
        return filteredProducts
    }

    fun filterProductsByPriceRange(minPrice: Double, maxPrice: Double): List<Product> {
        val currentState = getCurrentState()
        val filteredProducts = currentState.products.filter {
            it.price >= minPrice && it.price <= maxPrice
        }

        Log.d("ProductManager", "Filter by price range $minPrice-$maxPrice: found ${filteredProducts.size} products")
        return filteredProducts
    }

    fun getProductsInCategory(categoryId: Int): List<Product> {
        val currentState = getCurrentState()
        val filteredProducts = currentState.products.filter { it.categoryId == categoryId }

        Log.d("ProductManager", "Local filter by category $categoryId: found ${filteredProducts.size} products")
        return filteredProducts
    }

    // =====================================================
    // PRODUCT STATISTICS METHODS
    // =====================================================

    fun getProductCount(): Int {
        val count = getCurrentState().products.size
        Log.d("ProductManager", "Total products: $count")
        return count
    }

    fun getCategoryCount(): Int {
        val count = getCurrentState().categories.size
        Log.d("ProductManager", "Total categories: $count")
        return count
    }

    fun getProductCountByCategory(categoryId: Int): Int {
        val count = getCurrentState().products.count { it.categoryId == categoryId }
        Log.d("ProductManager", "Products in category $categoryId: $count")
        return count
    }

    fun getAveragePrice(): Double {
        val products = getCurrentState().products
        val average = if (products.isNotEmpty()) {
            products.sumOf { it.price } / products.size
        } else {
            0.0
        }

        Log.d("ProductManager", "Average product price: $average TL")
        return average
    }

    fun getPriceRange(): Pair<Double, Double> {
        val products = getCurrentState().products
        val minPrice = products.minOfOrNull { it.price } ?: 0.0
        val maxPrice = products.maxOfOrNull { it.price } ?: 0.0

        Log.d("ProductManager", "Price range: $minPrice - $maxPrice TL")
        return Pair(minPrice, maxPrice)
    }

    // =====================================================
    // PRODUCT VALIDATION METHODS
    // =====================================================

    fun isProductDataLoaded(): Boolean {
        val isLoaded = getCurrentState().products.isNotEmpty()
        Log.d("ProductManager", "Product data loaded: $isLoaded")
        return isLoaded
    }

    fun isCategoryDataLoaded(): Boolean {
        val isLoaded = getCurrentState().categories.isNotEmpty()
        Log.d("ProductManager", "Category data loaded: $isLoaded")
        return isLoaded
    }

    fun isValidCategoryId(categoryId: Int): Boolean {
        val isValid = getCurrentState().categories.any { it.id == categoryId }
        Log.d("ProductManager", "Category ID $categoryId is valid: $isValid")
        return isValid
    }

    fun isValidProductId(productId: Int): Boolean {
        val isValid = getCurrentState().products.any { it.id == productId }
        Log.d("ProductManager", "Product ID $productId is valid: $isValid")
        return isValid
    }

    // =====================================================
    // ADVANCED SEARCH METHODS
    // =====================================================

    fun searchProductsAdvanced(
        query: String? = null,
        categoryId: Int? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null
    ): List<Product> {
        var filteredProducts = getCurrentState().products

        // Apply name filter
        query?.let { searchTerm ->
            filteredProducts = filteredProducts.filter {
                it.name.contains(searchTerm, ignoreCase = true)
            }
        }

        // Apply category filter
        categoryId?.let { catId ->
            filteredProducts = filteredProducts.filter { it.categoryId == catId }
        }

        // Apply price range filter
        minPrice?.let { min ->
            filteredProducts = filteredProducts.filter { it.price >= min }
        }
        maxPrice?.let { max ->
            filteredProducts = filteredProducts.filter { it.price <= max }
        }

        Log.d("ProductManager", "Advanced search: found ${filteredProducts.size} products")
        Log.d("ProductManager", "  - Query: ${query ?: "none"}")
        Log.d("ProductManager", "  - Category: ${categoryId ?: "none"}")
        Log.d("ProductManager", "  - Price range: ${minPrice ?: "none"} - ${maxPrice ?: "none"}")

        return filteredProducts
    }
}