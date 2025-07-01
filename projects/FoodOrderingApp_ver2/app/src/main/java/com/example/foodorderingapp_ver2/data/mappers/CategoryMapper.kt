
// data/mappers/CategoryMapper.kt
package com.example.foodorderingapp_ver2.data.mappers

import com.example.foodorderingapp_ver2.domain.entities.Category
import com.example.foodorderingapp_ver2.data.remote.CategoryDto

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