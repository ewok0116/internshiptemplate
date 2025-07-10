// presentation/di/FoodOrderingViewModelFactory.kt
package com.example.foodorderingapp_ver2.presentation.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.foodorderingapp_ver2.domain.usecases.*
import com.example.foodorderingapp_ver2.presentation.viewmodel.FoodOrderingViewModel

class FoodOrderingViewModelFactory(
    private val getProductsUseCase: GetProductsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val createOrderUseCase: CreateOrderUseCase,
    private val testConnectionUseCase: TestConnectionUseCase,
    private val initializeConnectionUseCase: InitializeConnectionUseCase,
    private val onShowToast: (String) -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodOrderingViewModel::class.java)) {
            return FoodOrderingViewModel(
                getProductsUseCase = getProductsUseCase,
                getCategoriesUseCase = getCategoriesUseCase,
                searchProductsUseCase = searchProductsUseCase,
                getProductsByCategoryUseCase = getProductsByCategoryUseCase,
                createOrderUseCase = createOrderUseCase,
                testConnectionUseCase = testConnectionUseCase,
                initializeConnectionUseCase = initializeConnectionUseCase,
                onShowToast = onShowToast
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}