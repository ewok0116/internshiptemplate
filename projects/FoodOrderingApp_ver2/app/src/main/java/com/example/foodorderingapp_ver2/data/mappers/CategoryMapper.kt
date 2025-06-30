
// data/mappers/CategoryMapper.kt
package com.example.foodorderingapp.data.mappers

import com.example.foodorderingapp.domain.entities.Category
import com.example.foodorderingapp.data.remote.CategoryDto

object CategoryMapper {
    fun mapToDomain(dto: CategoryDto): Category {
        return Category(
            id = dto.id,
            name = dto.name,
            description = dto.description
        )
    }

    fun mapToDomainList(dtoList: List<CategoryDto>): List<Category> {
        return dtoList.map { mapToDomain(it) }
    }
}