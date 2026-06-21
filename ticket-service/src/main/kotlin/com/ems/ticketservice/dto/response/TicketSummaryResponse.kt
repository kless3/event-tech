package com.ems.ticketservice.dto.response

import java.io.Serializable
import java.util.UUID

data class TicketSummaryResponse(
    val eventId: UUID,
    val activeTickets: Long,
    val reservedTickets: Long,
) : Serializable
