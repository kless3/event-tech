package com.ems.notificationservice.messaging

import com.ems.notificationservice.config.KafkaTopicsProperties
import com.ems.notificationservice.domain.ProcessedKafkaMessage
import com.ems.notificationservice.dto.event.EventCancelledEvent
import com.ems.notificationservice.repository.ProcessedKafkaMessageRepository
import com.ems.notificationservice.service.NotificationService
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.mockito.Mockito
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.module.kotlin.jacksonMapperBuilder

class EventCancelledConsumerTest {
    private val objectMapper = jacksonMapperBuilder().build()
    private val notificationService = Mockito.mock(NotificationService::class.java)
    private val processedMessages = Mockito.mock(ProcessedKafkaMessageRepository::class.java)
    @Suppress("UNCHECKED_CAST")
    private val kafkaTemplate = Mockito.mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>
    private val topics = KafkaTopicsProperties(
        paymentSucceeded = "ems.payment.succeeded",
        paymentFailed = "ems.payment.failed",
        eventCancelled = "ems.event.cancelled",
        deadLetterSuffix = ".DLT",
    )
    private val consumer = EventCancelledConsumer(objectMapper, notificationService, processedMessages, kafkaTemplate, topics)

    @Test
    fun `creates notification and stores processed message`() {
        val event = event()
        Mockito.`when`(processedMessages.existsById(event.eventId)).thenReturn(false)

        consumer.handle(record(objectMapper.writeValueAsString(event)))

        Mockito.verify(notificationService).notifyEventCancelled(event)
        Mockito.verify(processedMessages).save(Mockito.any(ProcessedKafkaMessage::class.java))
    }

    private fun event(): EventCancelledEvent =
        EventCancelledEvent(
            eventId = UUID.randomUUID(),
            cancelledEventId = UUID.randomUUID(),
            reason = "organizer cancelled",
            occurredAt = Instant.now(),
        )

    private fun record(value: String): ConsumerRecord<String, String> =
        ConsumerRecord("ems.event.cancelled", 0, 0, "event-key", value)
}
