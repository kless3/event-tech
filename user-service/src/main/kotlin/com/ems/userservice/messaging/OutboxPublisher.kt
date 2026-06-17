package com.ems.userservice.messaging

import com.ems.userservice.config.OutboxProperties
import com.ems.userservice.domain.OutboxEvent
import com.ems.userservice.domain.OutboxEventStatus
import com.ems.userservice.repository.OutboxEventRepository
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@ConditionalOnProperty(prefix = "app.outbox", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class OutboxPublisher(
    private val outboxEventRepository: OutboxEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val properties: OutboxProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${app.outbox.publish-delay-ms:2000}")
    @Transactional
    fun publishPendingEvents() {
        val events = outboxEventRepository.findAllByStatusOrderByCreatedAtAsc(
            OutboxEventStatus.PENDING,
            PageRequest.of(0, properties.publishLimit),
        )
        events.forEach(::publish)
    }

    private fun publish(event: OutboxEvent) {
        try {
            kafkaTemplate.send(event.topic, event.messageKey, event.payload).get(PUBLISH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            event.markPublished(LocalDateTime.now())
        } catch (exception: Exception) {
            log.warn("Failed to publish outbox event {}", event.id, exception)
            event.markPublishFailed(exception.message ?: "Kafka publish failed", properties.maxAttempts)
        }
    }

    private companion object {
        const val PUBLISH_TIMEOUT_SECONDS = 5L
    }
}
