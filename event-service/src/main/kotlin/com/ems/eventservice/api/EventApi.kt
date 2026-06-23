package com.ems.eventservice.api

import com.ems.eventservice.dto.request.CreateEventRequest
import com.ems.eventservice.dto.response.EventAvailabilityResponse
import com.ems.eventservice.dto.response.EventResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.ResponseEntity

@Tag(name = "Events", description = "Public event catalog and internal event lifecycle endpoints")
interface EventApi {
    @Operation(summary = "Create an event through an internal service-to-service API")
    @ApiResponse(responseCode = "201", description = "Event created")
    @ApiResponse(responseCode = "400", description = "Invalid event payload")
    suspend fun createEvent(@Valid request: CreateEventRequest): ResponseEntity<EventResponse>

    @Operation(summary = "Get event details")
    @ApiResponse(responseCode = "200", description = "Event found")
    @ApiResponse(responseCode = "404", description = "Event not found")
    suspend fun getEvent(@Parameter(description = "Event id") id: UUID): EventResponse

    @Operation(summary = "Get event ticket availability")
    @ApiResponse(responseCode = "200", description = "Availability calculated")
    @ApiResponse(responseCode = "404", description = "Event not found")
    suspend fun getAvailability(
        @Parameter(description = "Event id") id: UUID,
    ): EventAvailabilityResponse

    @Operation(summary = "Cancel an event")
    @ApiResponse(responseCode = "200", description = "Event cancelled")
    @ApiResponse(responseCode = "404", description = "Event not found")
    @ApiResponse(responseCode = "409", description = "Event cannot be cancelled")
    suspend fun cancelEvent(@Parameter(description = "Event id") id: UUID): EventResponse
}
