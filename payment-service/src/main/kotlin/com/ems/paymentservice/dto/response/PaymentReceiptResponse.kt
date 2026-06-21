package com.ems.paymentservice.dto.response

import java.time.LocalDateTime
import java.util.UUID

data class PaymentReceiptResponse(
    val paymentId: UUID,
    val ticketId: UUID,
    val receiptObjectKey: String,
    val receiptUrl: String,
    val paidAt: LocalDateTime,
)
