package com.ems.paymentservice.messaging

import com.ems.paymentservice.config.KafkaTopicsProperties
import com.ems.paymentservice.domain.ProcessedKafkaMessage
import com.ems.paymentservice.dto.event.TicketCreatedEvent
import com.ems.paymentservice.repository.ProcessedKafkaMessageRepository
import com.ems.paymentservice.service.PaymentService
import java.time.LocalDateTime
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

@Component
class TicketCreatedConsumer(
    private val objectMapper: ObjectMapper,
    private val paymentService: PaymentService,
    private val processedKafkaMessageRepository: ProcessedKafkaMessageRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val topics: KafkaTopicsProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["\${app.kafka.topics.ticket-created}"])
    @Transactional
    fun handle(record: ConsumerRecord<String, String>) {
        val event = parseEventOrSendToDlt(record) ?: return
        if (event.eventType != TICKET_CREATED_EVENT_TYPE) {
            sendToDlt(record, "Unexpected eventType '${event.eventType}'")
            return
        }
        if (processedKafkaMessageRepository.existsById(event.eventId)) {
            log.info("Skipping already processed Kafka event {}", event.eventId)
            return
        }

        paymentService.createPaymentForTicket(event)
        processedKafkaMessageRepository.save(
            ProcessedKafkaMessage(
                messageId = event.eventId,
                topic = record.topic(),
                messageKey = record.key().orEmpty(),
                processedAt = LocalDateTime.now(),
            ),
        )
    }

    private fun parseEventOrSendToDlt(record: ConsumerRecord<String, String>): TicketCreatedEvent? =
        try {
            objectMapper.readValue(record.value(), TicketCreatedEvent::class.java)
        } catch (exception: Exception) {
            sendToDlt(record, "Unable to deserialize ticket.created event: ${exception.message}")
            null
        }

    private fun sendToDlt(record: ConsumerRecord<String, String>, reason: String) {
        log.warn("Sending Kafka message from topic {} to DLT: {}", record.topic(), reason)
        kafkaTemplate.send(topics.deadLetterTopic(record.topic()), record.key(), record.value())
    }

    private companion object {
        const val TICKET_CREATED_EVENT_TYPE = "ticket.created"
    }
}
