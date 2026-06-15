package com.ems.ticketservice.service

import com.ems.ticketservice.client.UserKeyClient
import com.ems.ticketservice.crypto.TicketCryptoService
import com.ems.ticketservice.domain.Ticket
import com.ems.ticketservice.domain.TicketStatus
import com.ems.ticketservice.dto.request.CreateTicketRequest
import com.ems.ticketservice.dto.response.TicketResponse
import com.ems.ticketservice.exception.TicketErasedException
import com.ems.ticketservice.exception.TicketNotFoundException
import com.ems.ticketservice.mapper.toResponse
import com.ems.ticketservice.messaging.OutboxEventFactory
import com.ems.ticketservice.repository.OutboxEventRepository
import com.ems.ticketservice.repository.TicketRepository
import java.time.LocalDateTime
import java.util.UUID
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
) {
    @Transactional
    fun createTicket(request: CreateTicketRequest): TicketResponse {
        val userId = requireNotNull(request.userId) { "userId must not be null" }
        val eventId = requireNotNull(request.eventId) { "eventId must not be null" }
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

        outboxEventRepository.save(outboxEventFactory.ticketCreated(ticket.id, userId, eventId))
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
        return ticket.toResponse(decryptPayload(ticket))
    }

    @Transactional
    fun eraseTicketsForUser(userId: UUID): List<UUID> {
        val erasedAt = LocalDateTime.now()
        val tickets = ticketRepository.findAllByUserIdAndStatus(userId, TicketStatus.ACTIVE)
        if (tickets.isEmpty()) {
            return emptyList()
        }

        tickets.forEach { ticket -> ticket.eraseForGdpr(erasedAt) }
        val erasedTicketIds = tickets.map { ticket -> ticket.id }
        outboxEventRepository.save(outboxEventFactory.ticketGdprErased(userId, erasedTicketIds))
        return erasedTicketIds
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
}
