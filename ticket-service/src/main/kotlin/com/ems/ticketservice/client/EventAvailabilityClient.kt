package com.ems.ticketservice.client

import com.ems.ticketservice.dto.response.EventAvailabilityResponse
import com.ems.ticketservice.exception.EventServiceUnavailableException
import com.ems.ticketservice.exception.EventUnavailableException
import java.util.UUID
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException

fun interface EventAvailabilityClient {
    fun ensureEventCanReserveTicket(eventId: UUID)
}

@Component
class HttpEventAvailabilityClient(
    @Qualifier("eventServiceRestClient")
    private val eventServiceRestClient: RestClient,
) : EventAvailabilityClient {
    override fun ensureEventCanReserveTicket(eventId: UUID) {
        val availability = getAvailability(eventId)
        if (availability.remainingCapacity <= 0) {
            throw EventUnavailableException(eventId)
        }
    }

    private fun getAvailability(eventId: UUID): EventAvailabilityResponse =
        try {
            eventServiceRestClient.get()
                .uri("/api/v1/events/{id}/availability", eventId)
                .retrieve()
                .body(EventAvailabilityResponse::class.java)
                ?: throw EventServiceUnavailableException("Event Service returned an empty availability response")
        } catch (exception: RestClientResponseException) {
            when (exception.statusCode) {
                HttpStatus.NOT_FOUND,
                HttpStatus.CONFLICT -> throw EventUnavailableException(eventId)
                else -> throw EventServiceUnavailableException("Event Service request failed", exception)
            }
        } catch (exception: RestClientException) {
            throw EventServiceUnavailableException("Event Service is unavailable", exception)
        }
}
