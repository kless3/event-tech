package com.ems.eventservice.dto.event

import java.time.Instant
import java.util.UUID

data class TicketCreatedEvent(
    val eventId: UUID,
    val eventType: String,
    val ticketId: UUID,
    val userId: UUID,
    val eventIdRef: UUID,
    val occurredAt: Instant,
)
