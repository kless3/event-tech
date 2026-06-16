package com.ems.eventservice.dto.event

import java.time.Instant
import java.util.UUID

data class UserDeletedEvent(
    val eventId: UUID,
    val eventType: String,
    val userId: UUID,
    val occurredAt: Instant,
)
