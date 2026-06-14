package com.ems.userservice.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateUserRequest(
    @field:NotBlank(message = "email must not be blank")
    @field:Email(message = "email must be a valid email address")
    val email: String,
)
