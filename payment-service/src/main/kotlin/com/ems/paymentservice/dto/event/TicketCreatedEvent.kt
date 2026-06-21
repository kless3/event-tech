package com.ems.paymentservice.dto.event

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class TicketCreatedEvent(
    val eventId: UUID,
    val eventType: String = "ticket.created",
    val ticketId: UUID,
    val userId: UUID,
    val eventIdRef: UUID,
    val amount: BigDecimal,
    val currency: String,
    val occurredAt: Instant,
)
