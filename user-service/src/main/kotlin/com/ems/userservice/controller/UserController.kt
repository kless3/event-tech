package com.ems.userservice.controller

import com.ems.userservice.dto.CreateUserRequest
import com.ems.userservice.dto.DecryptedKeyResponse
import com.ems.userservice.dto.UserResponse
import com.ems.userservice.service.UserService
import jakarta.validation.Valid
import java.net.URI
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val response = userService.createUser(request.email)
        return ResponseEntity
            .created(URI.create("/api/v1/users/${response.id}"))
            .body(response)
    }

    @GetMapping("/{id}/decrypt-key")
    fun getUserDecryptedKey(@PathVariable id: UUID): DecryptedKeyResponse =
        userService.getUserDecryptedKey(id)

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<Void> {
        userService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }
}
