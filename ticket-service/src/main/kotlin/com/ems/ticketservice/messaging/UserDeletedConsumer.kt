package com.ems.ticketservice.messaging

import com.ems.ticketservice.config.KafkaTopicsProperties
import com.ems.ticketservice.domain.ProcessedKafkaMessage
import com.ems.ticketservice.dto.event.UserDeletedEvent
import com.ems.ticketservice.repository.ProcessedKafkaMessageRepository
import com.ems.ticketservice.service.TicketService
import java.time.LocalDateTime
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Component
class UserDeletedConsumer(
    private val objectMapper: ObjectMapper,
    private val ticketService: TicketService,
    private val processedKafkaMessageRepository: ProcessedKafkaMessageRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val topics: KafkaTopicsProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["\${app.kafka.topics.user-deleted}"])
    @Transactional
    fun handle(record: ConsumerRecord<String, String>) {
        val event = parseEventOrSendToDlt(record) ?: return
        if (event.eventType != USER_DELETED_EVENT_TYPE) {
            sendToDlt(record, "Unexpected eventType '${event.eventType}'")
            return
        }
        if (processedKafkaMessageRepository.existsById(event.eventId)) {
            log.info("Skipping already processed Kafka event {}", event.eventId)
            return
        }

        ticketService.eraseTicketsForUser(event.userId)
        processedKafkaMessageRepository.save(
            ProcessedKafkaMessage(
                messageId = event.eventId,
                topic = record.topic(),
                messageKey = record.key().orEmpty(),
                processedAt = LocalDateTime.now(),
            ),
        )
    }

    private fun parseEventOrSendToDlt(record: ConsumerRecord<String, String>): UserDeletedEvent? =
        try {
            objectMapper.readValue(record.value(), UserDeletedEvent::class.java)
        } catch (exception: Exception) {
            sendToDlt(record, "Unable to deserialize user.deleted event: ${exception.message}")
            null
        }

    private fun sendToDlt(record: ConsumerRecord<String, String>, reason: String) {
        log.warn("Sending Kafka message from topic {} to DLT: {}", record.topic(), reason)
        kafkaTemplate.send(topics.deadLetterTopic(record.topic()), record.key(), record.value())
    }

    private companion object {
        const val USER_DELETED_EVENT_TYPE = "user.deleted"
    }
}
