
// data/mappers/UserMapper.kt
package com.example.foodorderingapp_ver2.data.mappers

import com.example.foodorderingapp_ver2.domain.entities.User
import com.example.foodorderingapp_ver2.data.remote.UserDto

object UserMapper {
    fun mapToDomain(dto: UserDto): User {
        return User(
            id = dto.id,
            name = dto.name,
            email = dto.email
        )
    }

    fun mapToDomainList(dtoList: List<UserDto>): List<User> {
        return dtoList.map { mapToDomain(it) }
    }
}
