package com.ems.eventservice.service

import com.ems.eventservice.client.TicketSummaryClient
import com.ems.eventservice.client.UserKeyClient
import com.ems.eventservice.config.KafkaTopicsProperties
import com.ems.eventservice.crypto.EventCryptoService
import com.ems.eventservice.domain.Event
import com.ems.eventservice.domain.EventStatus
import com.ems.eventservice.domain.OutboxEvent
import com.ems.eventservice.dto.request.CreateEventRequest
import com.ems.eventservice.dto.response.TicketSummaryResponse
import com.ems.eventservice.dto.response.UserKeyResponse
import com.ems.eventservice.exception.EventUnavailableException
import com.ems.eventservice.messaging.OutboxEventFactory
import com.ems.eventservice.repository.EventRepository
import com.ems.eventservice.repository.OutboxEventRepository
import java.time.LocalDateTime
import java.util.Base64
import java.util.Optional
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import tools.jackson.module.kotlin.jacksonMapperBuilder

class EventServiceTest {
    private val eventRepository = Mockito.mock(EventRepository::class.java)
    private val outboxEventRepository = Mockito.mock(OutboxEventRepository::class.java)
    private val userKeyClient = Mockito.mock(UserKeyClient::class.java)
    private val ticketSummaryClient = Mockito.mock(TicketSummaryClient::class.java)
    private val cryptoService = EventCryptoService()
    private val objectMapper = jacksonMapperBuilder().build()
    private val outboxEventFactory = OutboxEventFactory(
        objectMapper = objectMapper,
        topics = KafkaTopicsProperties(
            userDeleted = "ems.user.deleted",
            ticketCreated = "ems.ticket.created",
            eventCreated = "ems.event.created",
            eventCancelled = "ems.event.cancelled",
            deadLetterSuffix = ".DLT",
        ),
    )
    private val eventService = EventService(
        eventRepository = eventRepository,
        outboxEventRepository = outboxEventRepository,
        userKeyClient = userKeyClient,
        ticketSummaryClient = ticketSummaryClient,
        eventCryptoService = cryptoService,
        outboxEventFactory = outboxEventFactory,
    )
    private val dekBase64 = Base64.getEncoder().encodeToString(ByteArray(32) { 8 })

    @Test
    fun `creates event with encrypted organizer note and outbox event`() {
        val organizerUserId = UUID.randomUUID()
        Mockito.`when`(userKeyClient.getUserDek(organizerUserId)).thenReturn(UserKeyResponse(organizerUserId, dekBase64))
        Mockito.`when`(eventRepository.save(Mockito.any(Event::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Event>(0)
        }

        val response = eventService.createEvent(
            CreateEventRequest(
                organizerUserId = organizerUserId,
                title = " Kotlin Conf ",
                description = " Tech event ",
                location = " Warsaw ",
                startsAt = LocalDateTime.now().plusDays(10),
                capacity = 100,
                organizerNote = "private note",
            ),
        )

        val eventCaptor = ArgumentCaptor.forClass(Event::class.java)
        Mockito.verify(eventRepository).save(eventCaptor.capture())
        val savedEvent = eventCaptor.value

        assertEquals("Kotlin Conf", response.title)
        assertEquals("Warsaw", response.location)
        assertEquals("private note", response.organizerNote)
        assertNotNull(savedEvent.encryptedOrganizerNote)
        assertNotNull(savedEvent.organizerNoteIv)
        val outboxCaptor = ArgumentCaptor.forClass(OutboxEvent::class.java)
        Mockito.verify(outboxEventRepository).save(outboxCaptor.capture())
        assertEquals("event.created", outboxCaptor.value.eventType)
    }

    @Test
    fun `returns availability from ticket service summary`() {
        val event = event(capacity = 10)
        Mockito.`when`(eventRepository.findById(event.id)).thenReturn(Optional.of(event))
        Mockito.`when`(ticketSummaryClient.getTicketSummary(event.id)).thenReturn(TicketSummaryResponse(event.id, 4))

        val response = eventService.getAvailability(event.id)

        assertEquals(10, response.capacity)
        assertEquals(4, response.activeTickets)
        assertEquals(6, response.remainingCapacity)
    }

    @Test
    fun `registers ticket created`() {
        val event = event(capacity = 2)
        Mockito.`when`(eventRepository.findById(event.id)).thenReturn(Optional.of(event))

        eventService.registerTicketCreated(event.id)

        assertEquals(1, event.ticketsSold)
    }

    @Test
    fun `rejects ticket created when event is sold out`() {
        val event = event(capacity = 1).apply { ticketsSold = 1 }
        Mockito.`when`(eventRepository.findById(event.id)).thenReturn(Optional.of(event))

        assertFailsWith<EventUnavailableException> {
            eventService.registerTicketCreated(event.id)
        }
    }

    @Test
    fun `erases organizer events and publishes cancellation`() {
        val organizerUserId = UUID.randomUUID()
        val events = listOf(event(organizerUserId = organizerUserId), event(organizerUserId = organizerUserId))
        Mockito.`when`(
            eventRepository.findAllByOrganizerUserIdAndStatusIn(
                organizerUserId,
                listOf(EventStatus.DRAFT, EventStatus.PUBLISHED),
            ),
        ).thenReturn(events)

        val erasedIds = eventService.eraseOrganizerEvents(organizerUserId)

        assertEquals(events.map { it.id }, erasedIds)
        events.forEach { event ->
            assertNull(event.organizerUserId)
            assertNull(event.encryptedOrganizerNote)
            assertNull(event.organizerNoteIv)
            assertEquals(EventStatus.ORGANIZER_ERASED, event.status)
            assertNotNull(event.gdprErasedAt)
        }
        Mockito.verify(outboxEventRepository, Mockito.times(2)).save(Mockito.any(OutboxEvent::class.java))
    }

    private fun event(
        organizerUserId: UUID = UUID.randomUUID(),
        capacity: Int = 100,
    ): Event = Event(
        organizerUserId = organizerUserId,
        title = "Kotlin Conf",
        description = "Tech event",
        location = "Warsaw",
        startsAt = LocalDateTime.now().plusDays(10),
        capacity = capacity,
        encryptedOrganizerNote = null,
        organizerNoteIv = null,
    )
}
