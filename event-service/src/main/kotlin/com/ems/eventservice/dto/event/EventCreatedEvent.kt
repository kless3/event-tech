package com.ems.eventservice.dto.event

import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

data class EventCreatedEvent(
    val eventId: UUID,
    val eventType: String = "event.created",
    val organizerUserId: UUID,
    val title: String,
    val startsAt: LocalDateTime,
    val capacity: Int,
    val occurredAt: Instant,
)
