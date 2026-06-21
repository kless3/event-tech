package com.ems.notificationservice.messaging

import com.ems.notificationservice.config.KafkaTopicsProperties
import com.ems.notificationservice.domain.ProcessedKafkaMessage
import com.ems.notificationservice.dto.event.PaymentFailedEvent
import com.ems.notificationservice.repository.ProcessedKafkaMessageRepository
import com.ems.notificationservice.service.NotificationService
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.mockito.Mockito
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.module.kotlin.jacksonMapperBuilder

class PaymentFailedConsumerTest {
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
    private val consumer = PaymentFailedConsumer(objectMapper, notificationService, processedMessages, kafkaTemplate, topics)

    @Test
    fun `creates notification and stores processed message`() {
        val event = event()
        Mockito.`when`(processedMessages.existsById(event.eventId)).thenReturn(false)

        consumer.handle(record(objectMapper.writeValueAsString(event)))

        Mockito.verify(notificationService).notifyPaymentFailed(event)
        Mockito.verify(processedMessages).save(Mockito.any(ProcessedKafkaMessage::class.java))
    }

    private fun event(): PaymentFailedEvent =
        PaymentFailedEvent(
            eventId = UUID.randomUUID(),
            paymentId = UUID.randomUUID(),
            ticketId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            sourceEventId = UUID.randomUUID(),
            amount = BigDecimal("49.90"),
            currency = "USD",
            reason = "insufficient funds",
            occurredAt = Instant.now(),
        )

    private fun record(value: String): ConsumerRecord<String, String> =
        ConsumerRecord("ems.payment.failed", 0, 0, "payment-key", value)
}
