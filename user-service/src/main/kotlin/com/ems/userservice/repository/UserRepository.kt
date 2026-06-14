package com.ems.userservice.repository

import com.ems.userservice.domain.UserEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun existsByEmail(email: String): Boolean
}
