package com.ems.ticketservice.dto.event

import java.time.Instant
import java.util.UUID

data class TicketCreatedEvent(
    val eventId: UUID,
    val eventType: String = "ticket.created",
    val ticketId: UUID,
    val userId: UUID,
    val eventIdRef: UUID,
    val occurredAt: Instant,
)
