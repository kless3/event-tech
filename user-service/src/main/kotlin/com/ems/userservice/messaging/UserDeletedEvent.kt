package com.ems.userservice.messaging

import java.time.Instant
import java.util.UUID

data class UserDeletedEvent(
    val eventId: UUID,
    val eventType: String = "user.deleted",
    val userId: UUID,
    val occurredAt: Instant,
)
