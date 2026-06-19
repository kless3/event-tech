package com.ems.paymentservice.receipt

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PaymentReceipt(
    val paymentId: UUID,
    val ticketId: UUID,
    val userId: UUID,
    val eventId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val paidAt: LocalDateTime,
)
