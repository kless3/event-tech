package com.ems.userservice.mapper

import com.ems.userservice.domain.UserEntity
import com.ems.userservice.dto.UserResponse

fun UserEntity.toResponse() = UserResponse(
    id = id,
    email = email,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
