package com.ems.eventservice.mapper

import com.ems.eventservice.domain.Event
import com.ems.eventservice.dto.response.EventResponse

fun Event.toResponse(organizerNote: String? = null) = EventResponse(
    id = id,
    organizerUserId = organizerUserId,
    title = title,
    description = description,
    location = location,
    startsAt = startsAt,
    capacity = capacity,
    ticketsSold = ticketsSold,
    status = status,
    organizerNote = organizerNote,
    createdAt = createdAt,
    updatedAt = updatedAt,
    cancelledAt = cancelledAt,
    gdprErasedAt = gdprErasedAt,
)
