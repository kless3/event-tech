package com.ems.paymentservice.dto.event

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class PaymentSucceededEvent(
    val eventId: UUID,
    val eventType: String = "payment.succeeded",
    val paymentId: UUID,
    val ticketId: UUID,
    val userId: UUID,
    val sourceEventId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val receiptObjectKey: String,
    val receiptUrl: String,
    val occurredAt: Instant,
)
