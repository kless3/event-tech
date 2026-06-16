package com.ems.eventservice.dto.response

import java.util.UUID

data class TicketSummaryResponse(
    val eventId: UUID,
    val activeTickets: Long,
)
