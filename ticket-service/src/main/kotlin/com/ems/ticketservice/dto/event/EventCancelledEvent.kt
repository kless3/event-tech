package com.ems.ticketservice.dto.event

import java.time.Instant
import java.util.UUID

data class EventCancelledEvent(
    val eventId: UUID,
    val eventType: String,
    val cancelledEventId: UUID,
    val reason: String,
    val occurredAt: Instant,
)
