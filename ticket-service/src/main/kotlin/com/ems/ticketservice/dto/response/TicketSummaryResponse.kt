package com.ems.ticketservice.dto.response

import java.util.UUID

data class TicketSummaryResponse(
    val eventId: UUID,
    val activeTickets: Long,
)
