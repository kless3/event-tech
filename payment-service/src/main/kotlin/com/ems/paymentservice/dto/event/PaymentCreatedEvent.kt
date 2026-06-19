package com.ems.paymentservice.dto.event

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PaymentCreatedEvent(
    val eventId: UUID,
    val eventType: String = "payment.created",
    val paymentId: UUID,
    val ticketId: UUID,
    val userId: UUID,
    val sourceEventId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val occurredAt: Instant,
)
