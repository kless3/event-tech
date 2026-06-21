package com.ems.eventservice.messaging

import com.ems.eventservice.config.KafkaTopicsProperties
import com.ems.eventservice.domain.ProcessedKafkaMessage
import com.ems.eventservice.dto.event.PaymentFailedEvent
import com.ems.eventservice.repository.ProcessedKafkaMessageRepository
import com.ems.eventservice.service.EventService
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
    private val eventService = Mockito.mock(EventService::class.java)
    private val processedMessages = Mockito.mock(ProcessedKafkaMessageRepository::class.java)
    @Suppress("UNCHECKED_CAST")
    private val kafkaTemplate = Mockito.mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>
    private val topics = KafkaTopicsProperties(
        userDeleted = "ems.user.deleted",
        ticketCreated = "ems.ticket.created",
        paymentFailed = "ems.payment.failed",
        eventCreated = "ems.event.created",
        eventCancelled = "ems.event.cancelled",
        deadLetterSuffix = ".DLT",
    )
    private val consumer = PaymentFailedConsumer(objectMapper, eventService, processedMessages, kafkaTemplate, topics)

    @Test
    fun `releases event reservation and stores processed message`() {
        val event = paymentFailedEvent()
        Mockito.`when`(processedMessages.existsById(event.eventId)).thenReturn(false)

        consumer.handle(record(objectMapper.writeValueAsString(event)))

        Mockito.verify(eventService).releaseTicketReservation(event.sourceEventId)
        Mockito.verify(processedMessages).save(Mockito.any(ProcessedKafkaMessage::class.java))
    }

    @Test
    fun `sends malformed message to dead letter topic`() {
        consumer.handle(record("{bad-json"))

        Mockito.verify(kafkaTemplate).send("ems.payment.failed.DLT", "payment-key", "{bad-json")
        Mockito.verifyNoInteractions(eventService)
    }

    private fun paymentFailedEvent(): PaymentFailedEvent =
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
