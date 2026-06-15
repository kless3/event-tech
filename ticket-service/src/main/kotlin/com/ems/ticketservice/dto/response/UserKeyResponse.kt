package com.ems.ticketservice.dto.response

import java.util.UUID

data class UserKeyResponse(
    val userId: UUID,
    val dekBase64: String,
)
