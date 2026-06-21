package com.ems.notificationservice.messaging

import com.ems.notificationservice.config.KafkaTopicsProperties
import com.ems.notificationservice.domain.ProcessedKafkaMessage
import com.ems.notificationservice.dto.event.PaymentSucceededEvent
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

class PaymentSucceededConsumerTest {
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
    private val consumer = PaymentSucceededConsumer(objectMapper, notificationService, processedMessages, kafkaTemplate, topics)

    @Test
    fun `creates notification and stores processed message`() {
        val event = event()
        Mockito.`when`(processedMessages.existsById(event.eventId)).thenReturn(false)

        consumer.handle(record(objectMapper.writeValueAsString(event)))

        Mockito.verify(notificationService).notifyPaymentSucceeded(event)
        Mockito.verify(processedMessages).save(Mockito.any(ProcessedKafkaMessage::class.java))
    }

    @Test
    fun `skips already processed message`() {
        val event = event()
        Mockito.`when`(processedMessages.existsById(event.eventId)).thenReturn(true)

        consumer.handle(record(objectMapper.writeValueAsString(event)))

        Mockito.verifyNoInteractions(notificationService)
    }

    @Test
    fun `sends malformed message to dead letter topic`() {
        consumer.handle(record("{bad-json"))

        Mockito.verify(kafkaTemplate).send("ems.payment.succeeded.DLT", "payment-key", "{bad-json")
        Mockito.verifyNoInteractions(notificationService)
    }

    private fun event(): PaymentSucceededEvent =
        PaymentSucceededEvent(
            eventId = UUID.randomUUID(),
            paymentId = UUID.randomUUID(),
            ticketId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            sourceEventId = UUID.randomUUID(),
            amount = BigDecimal("49.90"),
            currency = "USD",
            receiptObjectKey = "receipts/payment.pdf",
            receiptUrl = "http://localhost:4566/receipts/payment.pdf",
            occurredAt = Instant.now(),
        )

    private fun record(value: String): ConsumerRecord<String, String> =
        ConsumerRecord("ems.payment.succeeded", 0, 0, "payment-key", value)
}
