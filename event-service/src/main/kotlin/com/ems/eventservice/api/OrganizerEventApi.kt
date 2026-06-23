package com.ems.eventservice.api

import com.ems.eventservice.dto.request.CreateOrganizerEventRequest
import com.ems.eventservice.dto.response.EventResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.ResponseEntity

@Tag(name = "Organizer Events", description = "Event creation for the authenticated organizer")
interface OrganizerEventApi {
    @Operation(summary = "Create an event for the authenticated organizer")
    @ApiResponse(responseCode = "201", description = "Event created")
    @ApiResponse(responseCode = "400", description = "Invalid event payload")
    suspend fun createEvent(
        @Parameter(description = "Authenticated organizer user id from gateway") organizerUserId: UUID,
        @Valid request: CreateOrganizerEventRequest,
    ): ResponseEntity<EventResponse>
}
