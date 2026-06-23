package com.ems.ticketservice.api

import com.ems.ticketservice.dto.request.CreateTicketRequest
import com.ems.ticketservice.dto.response.TicketResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.ResponseEntity

@Tag(name = "Tickets", description = "Ticket reservation and cancellation endpoints")
interface TicketApi {
    @Operation(summary = "Reserve a ticket")
    @ApiResponse(responseCode = "201", description = "Ticket reserved")
    @ApiResponse(responseCode = "409", description = "Event is unavailable")
    suspend fun createTicket(@Valid request: CreateTicketRequest): ResponseEntity<TicketResponse>

    @Operation(summary = "Get ticket details")
    @ApiResponse(responseCode = "200", description = "Ticket found")
    @ApiResponse(responseCode = "404", description = "Ticket not found")
    suspend fun getTicket(@Parameter(description = "Ticket id") id: UUID): TicketResponse

    @Operation(summary = "Cancel a ticket")
    @ApiResponse(responseCode = "200", description = "Ticket cancelled")
    @ApiResponse(responseCode = "404", description = "Ticket not found")
    suspend fun cancelTicket(@Parameter(description = "Ticket id") id: UUID): TicketResponse
}
