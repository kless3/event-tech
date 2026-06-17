package com.ems.eventservice.controller

import com.ems.eventservice.domain.EventStatus
import com.ems.eventservice.dto.request.CreateOrganizerEventRequest
import com.ems.eventservice.dto.response.EventResponse
import com.ems.eventservice.service.EventService
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.mockito.Mockito
import org.springframework.http.HttpStatus

class OrganizerEventControllerTest {
    private val eventService = Mockito.mock(EventService::class.java)
    private val controller = OrganizerEventController(eventService)

    @Test
    fun `creates organizer event and returns created location`() {
        val organizerUserId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        val startsAt = LocalDateTime.now().plusDays(30)
        val request = CreateOrganizerEventRequest(
            title = "Event Summit",
            description = "Tech conference",
            location = "Berlin",
            startsAt = startsAt,
            capacity = 500,
            organizerNote = "speaker room setup",
        )
        val serviceResponse = EventResponse(
            id = eventId,
            organizerUserId = organizerUserId,
            title = request.title,
            description = request.description,
            location = request.location,
            startsAt = startsAt,
            capacity = request.capacity,
            ticketsSold = 0,
            status = EventStatus.PUBLISHED,
            organizerNote = request.organizerNote,
            createdAt = null,
            updatedAt = null,
            cancelledAt = null,
            gdprErasedAt = null,
        )
        Mockito.`when`(eventService.createOrganizerEvent(organizerUserId, request)).thenReturn(serviceResponse)

        val response = controller.createEvent(organizerUserId, request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("/api/v1/events/$eventId", response.headers.location?.toString())
        assertNotNull(response.body)
        assertEquals(organizerUserId, response.body?.organizerUserId)
        Mockito.verify(eventService).createOrganizerEvent(organizerUserId, request)
    }
}
