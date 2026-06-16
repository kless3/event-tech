package com.ems.eventservice.service

import com.ems.eventservice.client.TicketSummaryClient
import com.ems.eventservice.client.UserKeyClient
import com.ems.eventservice.crypto.EventCryptoService
import com.ems.eventservice.domain.Event
import com.ems.eventservice.domain.EventStatus
import com.ems.eventservice.dto.request.CreateEventRequest
import com.ems.eventservice.dto.response.EventAvailabilityResponse
import com.ems.eventservice.dto.response.EventResponse
import com.ems.eventservice.exception.EventNotFoundException
import com.ems.eventservice.exception.EventUnavailableException
import com.ems.eventservice.mapper.toResponse
import com.ems.eventservice.messaging.OutboxEventFactory
import com.ems.eventservice.repository.EventRepository
import com.ems.eventservice.repository.OutboxEventRepository
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EventService(
    private val eventRepository: EventRepository,
    private val outboxEventRepository: OutboxEventRepository,
    private val userKeyClient: UserKeyClient,
    private val ticketSummaryClient: TicketSummaryClient,
    private val eventCryptoService: EventCryptoService,
    private val outboxEventFactory: OutboxEventFactory,
) {
    @Transactional
    fun createEvent(request: CreateEventRequest): EventResponse {
        val organizerUserId = requireNotNull(request.organizerUserId) { "organizerUserId must not be null" }
        val startsAt = requireNotNull(request.startsAt) { "startsAt must not be null" }
        val userDek = userKeyClient.getUserDek(organizerUserId)
        val encryptedNote = request.organizerNote
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { eventCryptoService.encrypt(it, userDek.dekBase64) }

        val event = eventRepository.save(
            Event(
                organizerUserId = organizerUserId,
                title = request.title.trim(),
                description = request.description?.trim()?.takeIf { it.isNotBlank() },
                location = request.location.trim(),
                startsAt = startsAt,
                capacity = request.capacity,
                encryptedOrganizerNote = encryptedNote?.ciphertextBase64,
                organizerNoteIv = encryptedNote?.ivBase64,
            ),
        )
        outboxEventRepository.save(outboxEventFactory.eventCreated(event))
        return event.toResponse(request.organizerNote?.trim()?.takeIf { it.isNotBlank() })
    }

    @Transactional(readOnly = true)
    fun getEvent(id: UUID): EventResponse {
        val event = findEvent(id)
        return event.toResponse(decryptOrganizerNote(event))
    }

    @Transactional(readOnly = true)
    fun getAvailability(id: UUID): EventAvailabilityResponse {
        val event = findEvent(id)
        val summary = ticketSummaryClient.getTicketSummary(id)
        return EventAvailabilityResponse(
            eventId = event.id,
            capacity = event.capacity,
            ticketsSold = event.ticketsSold,
            activeTickets = summary.activeTickets,
            remainingCapacity = (event.capacity - summary.activeTickets).coerceAtLeast(0),
        )
    }

    @Transactional
    fun cancelEvent(id: UUID, reason: String = "event cancelled"): EventResponse {
        val event = findEvent(id)
        if (event.status == EventStatus.CANCELLED || event.status == EventStatus.ORGANIZER_ERASED) {
            throw EventUnavailableException(id)
        }
        event.cancel(LocalDateTime.now())
        outboxEventRepository.save(outboxEventFactory.eventCancelled(event.id, reason))
        return event.toResponse(decryptOrganizerNote(event))
    }

    @Transactional
    fun registerTicketCreated(eventId: UUID) {
        val event = findEvent(eventId)
        if (event.status != EventStatus.PUBLISHED) {
            throw EventUnavailableException(event.id)
        }
        if (event.ticketsSold >= event.capacity) {
            throw EventUnavailableException(event.id)
        }
        event.incrementTicketsSold()
    }

    @Transactional
    fun eraseOrganizerEvents(userId: UUID): List<UUID> {
        val events = eventRepository.findAllByOrganizerUserIdAndStatusIn(
            userId,
            listOf(EventStatus.DRAFT, EventStatus.PUBLISHED),
        )
        if (events.isEmpty()) {
            return emptyList()
        }

        val erasedAt = LocalDateTime.now()
        events.forEach { event ->
            event.eraseOrganizer(erasedAt)
            outboxEventRepository.save(outboxEventFactory.eventCancelled(event.id, "organizer erased for GDPR"))
        }
        return events.map { it.id }
    }

    private fun findEvent(id: UUID): Event =
        eventRepository.findById(id).orElseThrow { EventNotFoundException(id) }

    private fun decryptOrganizerNote(event: Event): String? {
        val encryptedNote = event.encryptedOrganizerNote ?: return null
        val noteIv = event.organizerNoteIv ?: return null
        val organizerUserId = event.organizerUserId ?: return null
        val userDek = userKeyClient.getUserDek(organizerUserId)
        return eventCryptoService.decrypt(encryptedNote, noteIv, userDek.dekBase64)
    }
}
