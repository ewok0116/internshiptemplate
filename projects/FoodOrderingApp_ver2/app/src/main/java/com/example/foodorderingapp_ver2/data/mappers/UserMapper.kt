
// data/mappers/UserMapper.kt
package com.example.foodorderingapp.data.mappers

import com.example.foodorderingapp.domain.entities.User
import com.example.foodorderingapp.data.remote.UserDto

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
