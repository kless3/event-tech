package com.ems.ticketservice.mapper

import com.ems.ticketservice.domain.Ticket
import com.ems.ticketservice.dto.response.TicketResponse
import com.ems.ticketservice.service.TicketPayload

fun Ticket.toResponse(payload: TicketPayload? = null) = TicketResponse(
    id = id,
    userId = userId,
    eventId = eventId,
    paymentId = paymentId,
    status = status,
    holderName = payload?.holderName,
    seatCode = payload?.seatCode,
    createdAt = createdAt,
    updatedAt = updatedAt,
    gdprErasedAt = gdprErasedAt,
)
