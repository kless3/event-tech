package com.ems.ticketservice.service

import com.ems.ticketservice.client.UserKeyClient
import com.ems.ticketservice.config.KafkaTopicsProperties
import com.ems.ticketservice.crypto.TicketCryptoService
import com.ems.ticketservice.domain.OutboxEvent
import com.ems.ticketservice.domain.Ticket
import com.ems.ticketservice.domain.TicketStatus
import com.ems.ticketservice.dto.request.CreateTicketRequest
import com.ems.ticketservice.dto.response.UserKeyResponse
import com.ems.ticketservice.exception.TicketErasedException
import com.ems.ticketservice.messaging.OutboxEventFactory
import com.ems.ticketservice.repository.OutboxEventRepository
import com.ems.ticketservice.repository.TicketRepository
import java.math.BigDecimal
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
import org.springframework.cache.CacheManager
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder

class TicketServiceTest {
    private val ticketRepository = Mockito.mock(TicketRepository::class.java)
    private val outboxEventRepository = Mockito.mock(OutboxEventRepository::class.java)
    private val userKeyClient = Mockito.mock(UserKeyClient::class.java)
    private val cacheManager = Mockito.mock(CacheManager::class.java)
    private val objectMapper: ObjectMapper = jacksonMapperBuilder().build()
    private val outboxEventFactory = OutboxEventFactory(
        objectMapper = objectMapper,
        topics = KafkaTopicsProperties(
            userDeleted = "ems.user.deleted",
            eventCancelled = "ems.event.cancelled",
            paymentSucceeded = "ems.payment.succeeded",
            paymentFailed = "ems.payment.failed",
            ticketCreated = "ems.ticket.created",
            ticketGdprErased = "ems.ticket.gdpr-erased",
            deadLetterSuffix = ".DLT",
        ),
    )
    private val cryptoService = TicketCryptoService()
    private val ticketService = TicketService(
        ticketRepository = ticketRepository,
        outboxEventRepository = outboxEventRepository,
        userKeyClient = userKeyClient,
        ticketCryptoService = cryptoService,
        outboxEventFactory = outboxEventFactory,
        objectMapper = objectMapper,
        cacheManager = cacheManager,
    )

    private val dekBase64 = Base64.getEncoder().encodeToString(ByteArray(32) { 9 })

    @Test
    fun `creates ticket with encrypted payload and outbox event`() {
        val userId = UUID.randomUUID()
        val eventId = UUID.randomUUID()
        Mockito.`when`(userKeyClient.getUserDek(userId)).thenReturn(UserKeyResponse(userId, dekBase64))
        Mockito.`when`(ticketRepository.save(Mockito.any(Ticket::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Ticket>(0)
        }

        val response = ticketService.createTicket(
            CreateTicketRequest(
                userId = userId,
                eventId = eventId,
                holderName = " Alice ",
                seatCode = " A-1 ",
                amount = BigDecimal("49.9"),
                currency = "USD",
            ),
        )

        val ticketCaptor = ArgumentCaptor.forClass(Ticket::class.java)
        Mockito.verify(ticketRepository).save(ticketCaptor.capture())
        val savedTicket = ticketCaptor.value

        assertEquals(userId, response.userId)
        assertEquals(eventId, response.eventId)
        assertEquals(TicketStatus.PENDING_PAYMENT, response.status)
        assertEquals("Alice", response.holderName)
        assertEquals("A-1", response.seatCode)
        assertNotNull(savedTicket.encryptedPayload)
        assertNotNull(savedTicket.payloadIv)
        assertEquals(12, Base64.getDecoder().decode(savedTicket.payloadIv).size)
        val outboxCaptor = ArgumentCaptor.forClass(OutboxEvent::class.java)
        Mockito.verify(outboxEventRepository).save(outboxCaptor.capture())
        assertEquals("ticket.created", outboxCaptor.value.eventType)
        assertEquals("ems.ticket.created", outboxCaptor.value.topic)
        assertEquals(true, outboxCaptor.value.payload.contains("\"amount\":49.90"))
        assertEquals(true, outboxCaptor.value.payload.contains("\"currency\":\"USD\""))
    }

    @Test
    fun `returns decrypted ticket`() {
        val userId = UUID.randomUUID()
        val ticket = encryptedTicket(userId = userId, payload = TicketPayload("Alice", "A-1"))
        Mockito.`when`(ticketRepository.findById(ticket.id)).thenReturn(Optional.of(ticket))
        Mockito.`when`(userKeyClient.getUserDek(userId)).thenReturn(UserKeyResponse(userId, dekBase64))

        val response = ticketService.getTicket(ticket.id)

        assertEquals(ticket.id, response.id)
        assertEquals("Alice", response.holderName)
        assertEquals("A-1", response.seatCode)
    }

    @Test
    fun `erases active tickets for deleted user`() {
        val userId = UUID.randomUUID()
        val tickets = listOf(
            encryptedTicket(userId = userId, payload = TicketPayload("Alice", "A-1")),
            encryptedTicket(userId = userId, payload = TicketPayload("Alice", "A-2")),
        )
        Mockito.`when`(ticketRepository.findAllByUserIdAndStatusIn(userId, erasureStatuses())).thenReturn(tickets)

        val erasedIds = ticketService.eraseTicketsForUser(userId)

        assertEquals(tickets.map { it.id }, erasedIds)
        tickets.forEach { ticket ->
            assertNull(ticket.userId)
            assertNull(ticket.encryptedPayload)
            assertNull(ticket.payloadIv)
            assertEquals(TicketStatus.USER_ERASED, ticket.status)
            assertNotNull(ticket.gdprErasedAt)
        }
        val outboxCaptor = ArgumentCaptor.forClass(OutboxEvent::class.java)
        Mockito.verify(outboxEventRepository).save(outboxCaptor.capture())
        assertEquals("ticket.gdpr-erased", outboxCaptor.value.eventType)
        assertEquals("ems.ticket.gdpr-erased", outboxCaptor.value.topic)
    }

    @Test
    fun `throws when ticket was erased`() {
        val ticket = Ticket(
            userId = null,
            eventId = UUID.randomUUID(),
            encryptedPayload = null,
            payloadIv = null,
            status = TicketStatus.USER_ERASED,
        )
        Mockito.`when`(ticketRepository.findById(ticket.id)).thenReturn(Optional.of(ticket))

        assertFailsWith<TicketErasedException> {
            ticketService.getTicket(ticket.id)
        }
    }

    @Test
    fun `activates pending ticket after successful payment`() {
        val ticket = encryptedTicket(userId = UUID.randomUUID(), payload = TicketPayload("Alice", "A-1"))
        val paymentId = UUID.randomUUID()
        Mockito.`when`(ticketRepository.findById(ticket.id)).thenReturn(Optional.of(ticket))

        ticketService.activateTicketAfterPayment(ticket.id, paymentId)

        assertEquals(TicketStatus.ACTIVE, ticket.status)
        assertEquals(paymentId, ticket.paymentId)
    }

    @Test
    fun `marks pending ticket as payment failed`() {
        val ticket = encryptedTicket(userId = UUID.randomUUID(), payload = TicketPayload("Alice", "A-1"))
        val paymentId = UUID.randomUUID()
        Mockito.`when`(ticketRepository.findById(ticket.id)).thenReturn(Optional.of(ticket))

        ticketService.markTicketPaymentFailed(ticket.id, paymentId)

        assertEquals(TicketStatus.PAYMENT_FAILED, ticket.status)
        assertEquals(paymentId, ticket.paymentId)
    }

    private fun encryptedTicket(userId: UUID, payload: TicketPayload): Ticket {
        val encrypted = cryptoService.encrypt(objectMapper.writeValueAsString(payload), dekBase64)
        return Ticket(
            userId = userId,
            eventId = UUID.randomUUID(),
            encryptedPayload = encrypted.ciphertextBase64,
            payloadIv = encrypted.ivBase64,
        )
    }

    private fun erasureStatuses(): List<TicketStatus> =
        listOf(
            TicketStatus.PENDING_PAYMENT,
            TicketStatus.ACTIVE,
            TicketStatus.PAYMENT_FAILED,
        )

}
