package com.ems.eventservice.controller

import com.ems.eventservice.api.OrganizerEventApi
import com.ems.eventservice.dto.request.CreateOrganizerEventRequest
import com.ems.eventservice.dto.response.EventResponse
import com.ems.eventservice.service.EventService
import jakarta.validation.Valid
import java.net.URI
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/organizers/me/events")
class OrganizerEventController(
    private val eventService: EventService,
) : OrganizerEventApi {
    @PostMapping
    override suspend fun createEvent(
        @RequestHeader(AUTHENTICATED_USER_ID_HEADER) organizerUserId: UUID,
        @Valid @RequestBody request: CreateOrganizerEventRequest,
    ): ResponseEntity<EventResponse> =
        blockingEndpoint {
            val response = eventService.createOrganizerEvent(organizerUserId, request)
            ResponseEntity
                .created(URI.create("/api/v1/events/${response.id}"))
                .body(response)
        }

    private companion object {
        const val AUTHENTICATED_USER_ID_HEADER = "X-Authenticated-User-Id"
    }
}
