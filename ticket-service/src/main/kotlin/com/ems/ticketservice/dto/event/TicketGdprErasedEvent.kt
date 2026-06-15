package com.ems.ticketservice.dto.event

import java.time.Instant
import java.util.UUID

data class TicketGdprErasedEvent(
    val eventId: UUID,
    val eventType: String = "ticket.gdpr-erased",
    val userId: UUID,
    val erasedTicketIds: List<UUID>,
    val occurredAt: Instant,
)
