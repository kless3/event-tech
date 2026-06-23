package com.ems.ticketservice.api

import com.ems.ticketservice.dto.response.TicketSummaryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import java.util.UUID

@Tag(name = "Internal Ticket Summary", description = "Internal service-to-service ticket summary endpoints")
interface InternalTicketSummaryApi {
    @Operation(summary = "Get ticket summary for an event")
    @ApiResponse(responseCode = "200", description = "Ticket summary calculated")
    suspend fun getTicketSummary(
        @Parameter(description = "Event id") eventId: UUID,
    ): TicketSummaryResponse
}
