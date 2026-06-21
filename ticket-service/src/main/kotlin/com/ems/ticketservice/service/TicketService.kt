package com.ems.ticketservice.service

import com.ems.ticketservice.client.UserKeyClient
import com.ems.ticketservice.client.EventAvailabilityClient
import com.ems.ticketservice.config.CacheNames
import com.ems.ticketservice.crypto.TicketCryptoService
import com.ems.ticketservice.domain.Ticket
import com.ems.ticketservice.domain.TicketStatus
import com.ems.ticketservice.dto.request.CreateTicketRequest
import com.ems.ticketservice.dto.response.TicketSummaryResponse
import com.ems.ticketservice.dto.response.TicketResponse
import com.ems.ticketservice.exception.TicketErasedException
import com.ems.ticketservice.exception.TicketNotFoundException
import com.ems.ticketservice.mapper.toResponse
import com.ems.ticketservice.messaging.OutboxEventFactory
import com.ems.ticketservice.repository.OutboxEventRepository
import com.ems.ticketservice.repository.TicketRepository
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Service
class TicketService(
    private val ticketRepository: TicketRepository,
    private val outboxEventRepository: OutboxEventRepository,
    private val userKeyClient: UserKeyClient,
    private val ticketCryptoService: TicketCryptoService,
    private val outboxEventFactory: OutboxEventFactory,
    private val objectMapper: ObjectMapper,
    private val cacheManager: CacheManager,
    private val eventAvailabilityClient: EventAvailabilityClient,
) {
    @Transactional
    fun createTicket(request: CreateTicketRequest): TicketResponse {
        val userId = requireNotNull(request.userId) { "userId must not be null" }
        val eventId = requireNotNull(request.eventId) { "eventId must not be null" }
        val amount = requireNotNull(request.amount) { "amount must not be null" }.setScale(2, RoundingMode.HALF_UP)
        val currency = request.currency.trim().uppercase()
        eventAvailabilityClient.ensureEventCanReserveTicket(eventId)
        val userDek = userKeyClient.getUserDek(userId)
        val payload = TicketPayload(
            holderName = request.holderName.trim(),
            seatCode = request.seatCode.trim(),
        )
        val encryptedPayload = ticketCryptoService.encrypt(objectMapper.writeValueAsString(payload), userDek.dekBase64)
        val ticket = ticketRepository.save(
            Ticket(
                userId = userId,
                eventId = eventId,
                encryptedPayload = encryptedPayload.ciphertextBase64,
                payloadIv = encryptedPayload.ivBase64,
            ),
        )

        outboxEventRepository.save(outboxEventFactory.ticketCreated(ticket.id, userId, eventId, amount, currency))
        evictTicketSummary(eventId)
        return ticket.toResponse(payload)
    }

    @Transactional(readOnly = true)
    fun getTicket(id: UUID): TicketResponse {
        val ticket = findTicket(id)
        val payload = decryptPayload(ticket)
        return ticket.toResponse(payload)
    }

    @Transactional
    fun cancelTicket(id: UUID): TicketResponse {
        val ticket = findTicket(id)
        if (ticket.status == TicketStatus.USER_ERASED) {
            throw TicketErasedException(id)
        }
        ticket.cancel()
        evictTicketSummary(ticket.eventId)
        return ticket.toResponse(decryptPayload(ticket))
    }

    @Transactional
    fun eraseTicketsForUser(userId: UUID): List<UUID> {
        val erasedAt = LocalDateTime.now()
        val tickets = ticketRepository.findAllByUserIdAndStatusIn(userId, ERASABLE_TICKET_STATUSES)
        if (tickets.isEmpty()) {
            return emptyList()
        }

        tickets.forEach { ticket -> ticket.eraseForGdpr(erasedAt) }
        val erasedTicketIds = tickets.map { ticket -> ticket.id }
        outboxEventRepository.save(outboxEventFactory.ticketGdprErased(userId, erasedTicketIds))
        tickets.map { ticket -> ticket.eventId }.distinct().forEach(::evictTicketSummary)
        return erasedTicketIds
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = [CacheNames.TICKET_SUMMARIES], key = "#eventId")
    fun getTicketSummary(eventId: UUID): TicketSummaryResponse =
        TicketSummaryResponse(
            eventId = eventId,
            activeTickets = ticketRepository.countByEventIdAndStatus(eventId, TicketStatus.ACTIVE),
            reservedTickets = ticketRepository.countByEventIdAndStatusIn(eventId, RESERVED_TICKET_STATUSES),
        )

    @Transactional
    fun cancelActiveTicketsForEvent(eventId: UUID): List<UUID> {
        val tickets = ticketRepository.findAllByEventIdAndStatusIn(eventId, EVENT_CANCELLABLE_TICKET_STATUSES)
        tickets.forEach(Ticket::cancel)
        evictTicketSummary(eventId)
        return tickets.map { it.id }
    }

    @Transactional
    fun activateTicketAfterPayment(ticketId: UUID, paymentId: UUID) {
        val ticket = findTicket(ticketId)
        if (ticket.status == TicketStatus.ACTIVE && ticket.paymentId == paymentId) {
            return
        }
        if (ticket.status != TicketStatus.PENDING_PAYMENT) {
            return
        }

        ticket.activateAfterPayment(paymentId)
        evictTicketSummary(ticket.eventId)
    }

    @Transactional
    fun markTicketPaymentFailed(ticketId: UUID, paymentId: UUID) {
        val ticket = findTicket(ticketId)
        if (ticket.status == TicketStatus.PAYMENT_FAILED && ticket.paymentId == paymentId) {
            return
        }
        if (ticket.status != TicketStatus.PENDING_PAYMENT) {
            return
        }

        ticket.markPaymentFailed(paymentId)
        evictTicketSummary(ticket.eventId)
    }

    private fun findTicket(id: UUID): Ticket =
        ticketRepository.findById(id).orElseThrow { TicketNotFoundException(id) }

    private fun decryptPayload(ticket: Ticket): TicketPayload {
        if (ticket.status == TicketStatus.USER_ERASED) {
            throw TicketErasedException(ticket.id)
        }
        val userId = ticket.userId ?: throw TicketErasedException(ticket.id)
        val encryptedPayload = ticket.encryptedPayload ?: throw TicketErasedException(ticket.id)
        val payloadIv = ticket.payloadIv ?: throw TicketErasedException(ticket.id)
        val userDek = userKeyClient.getUserDek(userId)
        val decryptedPayload = ticketCryptoService.decrypt(encryptedPayload, payloadIv, userDek.dekBase64)
        return objectMapper.readValue(decryptedPayload, TicketPayload::class.java)
    }

    private fun evictTicketSummary(eventId: UUID) {
        cacheManager.getCache(CacheNames.TICKET_SUMMARIES)?.evict(eventId)
    }

    private companion object {
        val ERASABLE_TICKET_STATUSES = listOf(
            TicketStatus.PENDING_PAYMENT,
            TicketStatus.ACTIVE,
            TicketStatus.PAYMENT_FAILED,
        )
        val EVENT_CANCELLABLE_TICKET_STATUSES = listOf(
            TicketStatus.PENDING_PAYMENT,
            TicketStatus.ACTIVE,
        )
        val RESERVED_TICKET_STATUSES = listOf(
            TicketStatus.PENDING_PAYMENT,
            TicketStatus.ACTIVE,
        )
    }
}
