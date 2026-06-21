package com.ems.ticketservice.messaging

import com.ems.ticketservice.config.KafkaTopicsProperties
import com.ems.ticketservice.domain.OutboxEvent
import com.ems.ticketservice.dto.event.TicketCreatedEvent
import com.ems.ticketservice.dto.event.TicketGdprErasedEvent
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class OutboxEventFactory(
    private val objectMapper: ObjectMapper,
    private val topics: KafkaTopicsProperties,
) {
    fun ticketCreated(ticketId: UUID, userId: UUID, sourceEventId: UUID, amount: BigDecimal, currency: String): OutboxEvent {
        val event = TicketCreatedEvent(
            eventId = UUID.randomUUID(),
            ticketId = ticketId,
            userId = userId,
            eventIdRef = sourceEventId,
            amount = amount,
            currency = currency,
            occurredAt = Instant.now(),
        )
        return OutboxEvent(
            aggregateType = "ticket",
            aggregateId = ticketId,
            eventType = event.eventType,
            topic = topics.ticketCreated,
            messageKey = ticketId.toString(),
            payload = objectMapper.writeValueAsString(event),
        )
    }

    fun ticketGdprErased(userId: UUID, ticketIds: List<UUID>): OutboxEvent {
        val event = TicketGdprErasedEvent(
            eventId = UUID.randomUUID(),
            userId = userId,
            erasedTicketIds = ticketIds,
            occurredAt = Instant.now(),
        )
        return OutboxEvent(
            aggregateType = "user",
            aggregateId = userId,
            eventType = event.eventType,
            topic = topics.ticketGdprErased,
            messageKey = userId.toString(),
            payload = objectMapper.writeValueAsString(event),
        )
    }
}
