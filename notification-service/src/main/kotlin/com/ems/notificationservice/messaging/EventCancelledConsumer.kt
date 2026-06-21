package com.ems.notificationservice.messaging

import com.ems.notificationservice.config.KafkaTopicsProperties
import com.ems.notificationservice.domain.ProcessedKafkaMessage
import com.ems.notificationservice.dto.event.EventCancelledEvent
import com.ems.notificationservice.repository.ProcessedKafkaMessageRepository
import com.ems.notificationservice.service.NotificationService
import java.time.LocalDateTime
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Component
class EventCancelledConsumer(
    private val objectMapper: ObjectMapper,
    private val notificationService: NotificationService,
    private val processedKafkaMessageRepository: ProcessedKafkaMessageRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val topics: KafkaTopicsProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["\${app.kafka.topics.event-cancelled}"])
    @Transactional
    fun handle(record: ConsumerRecord<String, String>) {
        val event = parseEventOrSendToDlt(record) ?: return
        if (event.eventType != EVENT_CANCELLED_EVENT_TYPE) {
            sendToDlt(record, "Unexpected eventType '${event.eventType}'")
            return
        }
        if (processedKafkaMessageRepository.existsById(event.eventId)) {
            log.info("Skipping already processed Kafka event {}", event.eventId)
            return
        }

        notificationService.notifyEventCancelled(event)
        processedKafkaMessageRepository.save(
            ProcessedKafkaMessage(
                messageId = event.eventId,
                topic = record.topic(),
                messageKey = record.key().orEmpty(),
                processedAt = LocalDateTime.now(),
            ),
        )
    }

    private fun parseEventOrSendToDlt(record: ConsumerRecord<String, String>): EventCancelledEvent? =
        try {
            objectMapper.readValue(record.value(), EventCancelledEvent::class.java)
        } catch (exception: Exception) {
            sendToDlt(record, "Unable to deserialize event.cancelled event: ${exception.message}")
            null
        }

    private fun sendToDlt(record: ConsumerRecord<String, String>, reason: String) {
        log.warn("Sending Kafka message from topic {} to DLT: {}", record.topic(), reason)
        kafkaTemplate.send(topics.deadLetterTopic(record.topic()), record.key(), record.value())
    }

    private companion object {
        const val EVENT_CANCELLED_EVENT_TYPE = "event.cancelled"
    }
}
