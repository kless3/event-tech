package com.ems.eventservice.dto.response

import java.util.UUID

data class EventAvailabilityResponse(
    val eventId: UUID,
    val capacity: Int,
    val ticketsSold: Int,
    val activeTickets: Long,
    val remainingCapacity: Long,
)
