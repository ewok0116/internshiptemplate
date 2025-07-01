
// data/mappers/ProductMapper.kt
package com.example.foodorderingapp_ver2.data.mappers

import com.example.foodorderingapp_ver2.domain.entities.Product
import com.example.foodorderingapp_ver2.data.remote.ProductDto

object ProductMapper {
    fun mapToDomain(dto: ProductDto): Product {
        return Product(
            id = dto.id,
            name = dto.name,
            price = dto.price,
            description = dto.description,
            categoryId = dto.categoryId ?: 0,
            imageUrl = getEmojiForProduct(dto.name),
            isAvailable = dto.isAvailable ?: true
        )
    }

    fun mapToDomainList(dtoList: List<ProductDto>): List<Product> {
        return dtoList.map { mapToDomain(it) }
    }

    private fun getEmojiForProduct(productName: String): String {
        return when {
            productName.contains("burger", ignoreCase = true) -> "🍔"
            productName.contains("cola", ignoreCase = true) -> "🥤"
            productName.contains("juice", ignoreCase = true) -> "🧃"
            productName.contains("water", ignoreCase = true) -> "💧"
            productName.contains("coffee", ignoreCase = true) -> "☕"
            productName.contains("fries", ignoreCase = true) -> "🍟"
            productName.contains("nuggets", ignoreCase = true) -> "🍗"
            productName.contains("cheese", ignoreCase = true) -> "🧀"
            else -> "🍽️"
        }
    }
}