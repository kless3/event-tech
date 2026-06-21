package com.ems.ticketservice.messaging

import com.ems.ticketservice.config.KafkaTopicsProperties
import com.ems.ticketservice.domain.ProcessedKafkaMessage
import com.ems.ticketservice.dto.event.UserDeletedEvent
import com.ems.ticketservice.repository.ProcessedKafkaMessageRepository
import com.ems.ticketservice.service.TicketService
import java.time.Instant
import java.util.UUID
import kotlin.test.Test
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.mockito.Mockito
import org.springframework.kafka.core.KafkaTemplate
import tools.jackson.module.kotlin.jacksonMapperBuilder

class UserDeletedConsumerTest {
    private val objectMapper = jacksonMapperBuilder().build()
    private val ticketService = Mockito.mock(TicketService::class.java)
    private val processedMessages = Mockito.mock(ProcessedKafkaMessageRepository::class.java)
    private val kafkaTemplate = Mockito.mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>
    private val topics = KafkaTopicsProperties(
        userDeleted = "ems.user.deleted",
        eventCancelled = "ems.event.cancelled",
        paymentSucceeded = "ems.payment.succeeded",
        paymentFailed = "ems.payment.failed",
        ticketCreated = "ems.ticket.created",
        ticketGdprErased = "ems.ticket.gdpr-erased",
        deadLetterSuffix = ".DLT",
    )
    private val consumer = UserDeletedConsumer(
        objectMapper = objectMapper,
        ticketService = ticketService,
        processedKafkaMessageRepository = processedMessages,
        kafkaTemplate = kafkaTemplate,
        topics = topics,
    )

    @Test
    fun `erases tickets and stores processed message`() {
        val event = UserDeletedEvent(
            eventId = UUID.randomUUID(),
            eventType = "user.deleted",
            userId = UUID.randomUUID(),
            occurredAt = Instant.now(),
        )
        Mockito.`when`(processedMessages.existsById(event.eventId)).thenReturn(false)
        val record = record(objectMapper.writeValueAsString(event))

        consumer.handle(record)

        Mockito.verify(ticketService).eraseTicketsForUser(event.userId)
        Mockito.verify(processedMessages).save(Mockito.any(ProcessedKafkaMessage::class.java))
    }

    @Test
    fun `skips already processed message`() {
        val event = UserDeletedEvent(
            eventId = UUID.randomUUID(),
            eventType = "user.deleted",
            userId = UUID.randomUUID(),
            occurredAt = Instant.now(),
        )
        Mockito.`when`(processedMessages.existsById(event.eventId)).thenReturn(true)

        consumer.handle(record(objectMapper.writeValueAsString(event)))

        Mockito.verifyNoInteractions(ticketService)
    }

    @Test
    fun `sends malformed message to dead letter topic`() {
        consumer.handle(record("{not-json"))

        Mockito.verify(kafkaTemplate).send("ems.user.deleted.DLT", "user-key", "{not-json")
        Mockito.verifyNoInteractions(ticketService)
    }

    private fun record(value: String): ConsumerRecord<String, String> =
        ConsumerRecord("ems.user.deleted", 0, 0, "user-key", value)
}
