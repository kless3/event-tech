package com.ems.eventservice.dto.event

import java.time.Instant
import java.util.UUID

data class EventCancelledEvent(
    val eventId: UUID,
    val eventType: String = "event.cancelled",
    val cancelledEventId: UUID,
    val reason: String,
    val occurredAt: Instant,
)
