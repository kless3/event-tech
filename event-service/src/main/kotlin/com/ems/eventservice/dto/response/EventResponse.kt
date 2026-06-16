package com.ems.eventservice.dto.response

import com.ems.eventservice.domain.EventStatus
import java.time.LocalDateTime
import java.util.UUID

data class EventResponse(
    val id: UUID,
    val organizerUserId: UUID?,
    val title: String,
    val description: String?,
    val location: String,
    val startsAt: LocalDateTime,
    val capacity: Int,
    val ticketsSold: Int,
    val status: EventStatus,
    val organizerNote: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val cancelledAt: LocalDateTime?,
    val gdprErasedAt: LocalDateTime?,
)
