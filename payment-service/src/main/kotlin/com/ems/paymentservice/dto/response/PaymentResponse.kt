package com.ems.paymentservice.dto.response

import com.ems.paymentservice.domain.PaymentStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PaymentResponse(
    val id: UUID,
    val ticketId: UUID,
    val userId: UUID,
    val eventId: UUID,
    val amount: BigDecimal,
    val currency: String,
    val idempotencyKey: String,
    val status: PaymentStatus,
    val failureReason: String?,
    val receiptObjectKey: String?,
    val receiptUrl: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val paidAt: LocalDateTime?,
)
