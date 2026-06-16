package com.ems.eventservice.messaging

import com.ems.eventservice.config.KafkaTopicsProperties
import com.ems.eventservice.domain.ProcessedKafkaMessage
import com.ems.eventservice.dto.event.TicketCreatedEvent
import com.ems.eventservice.repository.ProcessedKafkaMessageRepository
import com.ems.eventservice.service.EventService
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.mockito.Mockito
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.module.kotlin.jacksonMapperBuilder

class TicketCreatedConsumerTest {
    private val objectMapper = jacksonMapperBuilder().build()
    private val eventService = Mockito.mock(EventService::class.java)
    private val processedMessages = Mockito.mock(ProcessedKafkaMessageRepository::class.java)
    @Suppress("UNCHECKED_CAST")
    private val kafkaTemplate = Mockito.mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>
    private val topics = topics()
    private val consumer = TicketCreatedConsumer(objectMapper, eventService, processedMessages, kafkaTemplate, topics)

    @Test
    fun `registers ticket created and stores processed message`() {
        val event = TicketCreatedEvent(
            eventId = UUID.randomUUID(),
            eventType = "ticket.created",
            ticketId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            eventIdRef = UUID.randomUUID(),
            occurredAt = Instant.now(),
        )
        Mockito.`when`(processedMessages.existsById(event.eventId)).thenReturn(false)

        consumer.handle(record(objectMapper.writeValueAsString(event)))

        Mockito.verify(eventService).registerTicketCreated(event.eventIdRef)
        Mockito.verify(processedMessages).save(Mockito.any(ProcessedKafkaMessage::class.java))
    }

    @Test
    fun `skips already processed message`() {
        val event = TicketCreatedEvent(UUID.randomUUID(), "ticket.created", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Instant.now())
        Mockito.`when`(processedMessages.existsById(event.eventId)).thenReturn(true)

        consumer.handle(record(objectMapper.writeValueAsString(event)))

        Mockito.verifyNoInteractions(eventService)
    }

    @Test
    fun `sends malformed message to dead letter topic`() {
        consumer.handle(record("{bad-json"))

        Mockito.verify(kafkaTemplate).send("ems.ticket.created.DLT", "ticket-key", "{bad-json")
        Mockito.verifyNoInteractions(eventService)
    }

    private fun record(value: String): ConsumerRecord<String, String> =
        ConsumerRecord("ems.ticket.created", 0, 0, "ticket-key", value)
}

private fun topics() = KafkaTopicsProperties(
    userDeleted = "ems.user.deleted",
    ticketCreated = "ems.ticket.created",
    eventCreated = "ems.event.created",
    eventCancelled = "ems.event.cancelled",
    deadLetterSuffix = ".DLT",
)
