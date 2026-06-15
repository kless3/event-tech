package com.ems.ticketservice.dto.response

import com.ems.ticketservice.domain.TicketStatus
import java.time.LocalDateTime
import java.util.UUID

data class TicketResponse(
    val id: UUID,
    val userId: UUID?,
    val eventId: UUID,
    val status: TicketStatus,
    val holderName: String?,
    val seatCode: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val gdprErasedAt: LocalDateTime?,
)
