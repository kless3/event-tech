package com.ems.eventservice.client

import com.ems.eventservice.dto.response.TicketSummaryResponse
import com.ems.eventservice.exception.TicketServiceUnavailableException
import java.util.UUID
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

interface TicketSummaryClient {
    fun getTicketSummary(eventId: UUID): TicketSummaryResponse
}

@Component
class HttpTicketSummaryClient(
    private val ticketServiceRestClient: RestClient,
) : TicketSummaryClient {
    override fun getTicketSummary(eventId: UUID): TicketSummaryResponse =
        try {
            ticketServiceRestClient.get()
                .uri("/internal/v1/events/{eventId}/ticket-summary", eventId)
                .retrieve()
                .body(TicketSummaryResponse::class.java)
                ?: throw TicketServiceUnavailableException("Ticket Service returned an empty summary response")
        } catch (exception: RestClientException) {
            throw TicketServiceUnavailableException("Ticket Service is unavailable", exception)
        }
}
