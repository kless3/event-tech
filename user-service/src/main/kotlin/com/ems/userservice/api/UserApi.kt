package com.ems.userservice.api

import com.ems.userservice.dto.request.CreateUserRequest
import com.ems.userservice.dto.response.DecryptedKeyResponse
import com.ems.userservice.dto.response.UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.ResponseEntity

@Tag(name = "Users", description = "User lifecycle and encryption key endpoints")
interface UserApi {
    @Operation(summary = "Create a user")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "409", description = "Email already exists")
    suspend fun createUser(@Valid request: CreateUserRequest): ResponseEntity<UserResponse>

    @Operation(summary = "Get decrypted data encryption key for a user")
    @ApiResponse(responseCode = "200", description = "User DEK decrypted")
    @ApiResponse(responseCode = "404", description = "User not found")
    suspend fun getUserDecryptedKey(
        @Parameter(description = "User id") id: UUID,
    ): DecryptedKeyResponse

    @Operation(summary = "Delete a user and publish GDPR deletion event")
    @ApiResponse(responseCode = "204", description = "User deleted")
    @ApiResponse(responseCode = "404", description = "User not found")
    suspend fun deleteUser(
        @Parameter(description = "User id") id: UUID,
    ): ResponseEntity<Unit>
}
