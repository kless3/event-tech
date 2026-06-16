package com.ems.eventservice.messaging

import com.ems.eventservice.config.KafkaTopicsProperties
import com.ems.eventservice.domain.ProcessedKafkaMessage
import com.ems.eventservice.dto.event.UserDeletedEvent
import com.ems.eventservice.repository.ProcessedKafkaMessageRepository
import com.ems.eventservice.service.EventService
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.mockito.Mockito
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.module.kotlin.jacksonMapperBuilder

class UserDeletedConsumerTest {
    private val objectMapper = jacksonMapperBuilder().build()
    private val eventService = Mockito.mock(EventService::class.java)
    private val processedMessages = Mockito.mock(ProcessedKafkaMessageRepository::class.java)
    @Suppress("UNCHECKED_CAST")
    private val kafkaTemplate = Mockito.mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>
    private val topics = KafkaTopicsProperties(
        userDeleted = "ems.user.deleted",
        ticketCreated = "ems.ticket.created",
        eventCreated = "ems.event.created",
        eventCancelled = "ems.event.cancelled",
        deadLetterSuffix = ".DLT",
    )
    private val consumer = UserDeletedConsumer(objectMapper, eventService, processedMessages, kafkaTemplate, topics)

    @Test
    fun `erases organizer events and stores processed message`() {
        val event = UserDeletedEvent(UUID.randomUUID(), "user.deleted", UUID.randomUUID(), Instant.now())
        Mockito.`when`(processedMessages.existsById(event.eventId)).thenReturn(false)

        consumer.handle(record(objectMapper.writeValueAsString(event)))

        Mockito.verify(eventService).eraseOrganizerEvents(event.userId)
        Mockito.verify(processedMessages).save(Mockito.any(ProcessedKafkaMessage::class.java))
    }

    @Test
    fun `sends malformed message to dead letter topic`() {
        consumer.handle(record("{bad-json"))

        Mockito.verify(kafkaTemplate).send("ems.user.deleted.DLT", "user-key", "{bad-json")
        Mockito.verifyNoInteractions(eventService)
    }

    private fun record(value: String): ConsumerRecord<String, String> =
        ConsumerRecord("ems.user.deleted", 0, 0, "user-key", value)
}
