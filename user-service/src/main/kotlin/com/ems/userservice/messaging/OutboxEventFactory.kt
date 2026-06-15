package com.ems.userservice.messaging

import com.ems.userservice.config.KafkaTopicsProperties
import com.ems.userservice.domain.OutboxEvent
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class OutboxEventFactory(
    private val objectMapper: ObjectMapper,
    private val topics: KafkaTopicsProperties,
) {
    fun userDeleted(userId: UUID): OutboxEvent {
        val event = UserDeletedEvent(
            eventId = UUID.randomUUID(),
            userId = userId,
            occurredAt = Instant.now(),
        )
        return OutboxEvent(
            aggregateType = "user",
            aggregateId = userId,
            eventType = event.eventType,
            topic = topics.userDeleted,
            messageKey = userId.toString(),
            payload = objectMapper.writeValueAsString(event),
        )
    }
}
