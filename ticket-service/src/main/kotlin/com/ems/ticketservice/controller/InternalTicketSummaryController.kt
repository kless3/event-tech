package com.ems.ticketservice.controller

import com.ems.ticketservice.api.InternalTicketSummaryApi
import com.ems.ticketservice.dto.response.TicketSummaryResponse
import com.ems.ticketservice.service.TicketService
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/events")
class InternalTicketSummaryController(
    private val ticketService: TicketService,
) : InternalTicketSummaryApi {
    @GetMapping("/{eventId}/ticket-summary")
    override suspend fun getTicketSummary(@PathVariable eventId: UUID): TicketSummaryResponse =
        blockingEndpoint { ticketService.getTicketSummary(eventId) }
}
