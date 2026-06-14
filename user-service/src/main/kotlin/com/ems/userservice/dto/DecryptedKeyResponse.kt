package com.ems.userservice.dto

import java.util.UUID

data class DecryptedKeyResponse(
    val userId: UUID,
    val dekBase64: String,
)
