// presentation/di/AppDependencies.kt - Final Version
package com.example.foodorderingapp_ver2.presentation.di

import com.example.foodorderingapp_ver2.data.remote.ApiClient
import com.example.foodorderingapp_ver2.data.repositories.*
import com.example.foodorderingapp_ver2.domain.repositories.*
import com.example.foodorderingapp_ver2.domain.usecases.*

class AppDependencies {

    // Repositories - Initialize ONLY when ApiClient is ready
    fun getProductRepository(): ProductRepository {
        return ProductRepositoryImpl(ApiClient.getApiService())
    }

    fun getCategoryRepository(): CategoryRepository {
        return CategoryRepositoryImpl(ApiClient.getApiService())
    }

    fun getOrderRepository(): OrderRepository {
        return OrderRepositoryImpl(ApiClient.getApiService())
    }

    fun getUserRepository(): UserRepository {
        return UserRepositoryImpl(ApiClient.getApiService())
    }

    // Connection repository doesn't need ApiClient for initialization
    val connectionRepository: ConnectionRepository by lazy {
        ConnectionRepositoryImpl()
    }

    // Use Cases - These create repositories on-demand
    fun getProductsUseCase(): GetProductsUseCase {
        return GetProductsUseCase(getProductRepository())
    }

    fun getCategoriesUseCase(): GetCategoriesUseCase {
        return GetCategoriesUseCase(getCategoryRepository())
    }

    fun searchProductsUseCase(): SearchProductsUseCase {
        return SearchProductsUseCase(getProductRepository())
    }

    fun getProductsByCategoryUseCase(): GetProductsByCategoryUseCase {
        return GetProductsByCategoryUseCase(getProductRepository())
    }

    fun createOrderUseCase(): CreateOrderUseCase {
        return CreateOrderUseCase(getOrderRepository())
    }

    fun testConnectionUseCase(): TestConnectionUseCase {
        return TestConnectionUseCase(connectionRepository)
    }

    fun initializeConnectionUseCase(): InitializeConnectionUseCase {
        return InitializeConnectionUseCase(connectionRepository)
    }
}