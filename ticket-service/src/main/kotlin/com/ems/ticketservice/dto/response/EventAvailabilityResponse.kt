package com.ems.ticketservice.dto.response

import java.util.UUID

data class EventAvailabilityResponse(
    val eventId: UUID,
    val capacity: Int,
    val ticketsSold: Int,
    val activeTickets: Long,
    val reservedTickets: Long,
    val remainingCapacity: Long,
)
