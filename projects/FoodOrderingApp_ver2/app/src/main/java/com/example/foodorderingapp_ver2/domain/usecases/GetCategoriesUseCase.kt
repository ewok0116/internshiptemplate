
// domain/usecases/GetCategoriesUseCase.kt
package com.example.foodorderingapp.domain.usecases

import com.example.foodorderingapp.domain.entities.Category
import com.example.foodorderingapp.domain.repositories.CategoryRepository
import com.example.foodorderingapp.domain.common.Result
import com.example.foodorderingapp.domain.common.NoParameterUseCase

class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) : NoParameterUseCase<List<Category>>() {

    override suspend fun invoke(): Result<List<Category>> {
        return categoryRepository.getCategories()
    }
}
