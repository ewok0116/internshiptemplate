
// domain/usecases/GetCategoriesUseCase.kt
package com.example.foodorderingapp_ver2.domain.usecases

import com.example.foodorderingapp_ver2.domain.entities.Category
import com.example.foodorderingapp_ver2.domain.repositories.CategoryRepository
import com.example.foodorderingapp_ver2.domain.common.Result
import com.example.foodorderingapp_ver2.domain.common.NoParameterUseCase

class GetCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) : NoParameterUseCase<List<Category>>() {

    override suspend fun invoke(): Result<List<Category>> {
        return categoryRepository.getCategories()
    }
}
