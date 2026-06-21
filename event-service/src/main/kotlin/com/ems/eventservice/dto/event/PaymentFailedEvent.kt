package com.ems.eventservice.dto.event

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PaymentFailedEvent(
    val eventId: UUID,
    val eventType: String = "payment.failed",
    val paymentId: UUID,
    val ticketId: UUID,
    val userId: UUID,
    val sourceEventId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val reason: String,
    val occurredAt: Instant,
)
