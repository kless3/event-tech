package com.ems.eventservice.messaging

import com.ems.eventservice.config.KafkaTopicsProperties
import com.ems.eventservice.domain.Event
import com.ems.eventservice.domain.OutboxEvent
import com.ems.eventservice.dto.event.EventCancelledEvent
import com.ems.eventservice.dto.event.EventCreatedEvent
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class OutboxEventFactory(
    private val objectMapper: ObjectMapper,
    private val topics: KafkaTopicsProperties,
) {
    fun eventCreated(event: Event): OutboxEvent {
        val organizerUserId = requireNotNull(event.organizerUserId) {
            "organizerUserId is required for event.created"
        }
        val payload = EventCreatedEvent(
            eventId = UUID.randomUUID(),
            organizerUserId = organizerUserId,
            title = event.title,
            startsAt = event.startsAt,
            capacity = event.capacity,
            occurredAt = Instant.now(),
        )
        return OutboxEvent(
            aggregateType = "event",
            aggregateId = event.id,
            eventType = payload.eventType,
            topic = topics.eventCreated,
            messageKey = event.id.toString(),
            payload = objectMapper.writeValueAsString(payload),
        )
    }

    fun eventCancelled(eventId: UUID, reason: String): OutboxEvent {
        val payload = EventCancelledEvent(
            eventId = UUID.randomUUID(),
            cancelledEventId = eventId,
            reason = reason,
            occurredAt = Instant.now(),
        )
        return OutboxEvent(
            aggregateType = "event",
            aggregateId = eventId,
            eventType = payload.eventType,
            topic = topics.eventCancelled,
            messageKey = eventId.toString(),
            payload = objectMapper.writeValueAsString(payload),
        )
    }
}
