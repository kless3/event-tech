package com.ems.paymentservice.mapper

import com.ems.paymentservice.domain.Payment
import com.ems.paymentservice.dto.response.PaymentResponse

fun Payment.toResponse(): PaymentResponse =
    PaymentResponse(
        id = id,
        ticketId = ticketId,
        userId = userId,
        eventId = eventId,
        amount = amount,
        currency = currency,
        idempotencyKey = idempotencyKey,
        status = status,
        failureReason = failureReason,
        receiptObjectKey = receiptObjectKey,
        receiptUrl = receiptUrl,
        createdAt = createdAt,
        updatedAt = updatedAt,
        paidAt = paidAt,
    )
