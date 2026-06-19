package com.ems.paymentservice.messaging

import com.ems.paymentservice.config.KafkaTopicsProperties
import com.ems.paymentservice.domain.OutboxEvent
import com.ems.paymentservice.domain.Payment
import com.ems.paymentservice.dto.event.PaymentCreatedEvent
import com.ems.paymentservice.dto.event.PaymentFailedEvent
import com.ems.paymentservice.dto.event.PaymentSucceededEvent
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class OutboxEventFactory(
    private val objectMapper: ObjectMapper,
    private val topics: KafkaTopicsProperties,
) {
    fun paymentCreated(payment: Payment): OutboxEvent {
        val event = PaymentCreatedEvent(
            eventId = UUID.randomUUID(),
            paymentId = payment.id,
            ticketId = payment.ticketId,
            userId = payment.userId,
            sourceEventId = payment.eventId,
            amount = payment.amount,
            currency = payment.currency,
            occurredAt = Instant.now(),
        )
        return paymentEvent(payment, event.eventType, topics.paymentCreated, objectMapper.writeValueAsString(event))
    }

    fun paymentSucceeded(payment: Payment): OutboxEvent {
        val event = PaymentSucceededEvent(
            eventId = UUID.randomUUID(),
            paymentId = payment.id,
            ticketId = payment.ticketId,
            userId = payment.userId,
            sourceEventId = payment.eventId,
            amount = payment.amount,
            currency = payment.currency,
            receiptObjectKey = requireNotNull(payment.receiptObjectKey),
            receiptUrl = requireNotNull(payment.receiptUrl),
            occurredAt = Instant.now(),
        )
        return paymentEvent(payment, event.eventType, topics.paymentSucceeded, objectMapper.writeValueAsString(event))
    }

    fun paymentFailed(payment: Payment): OutboxEvent {
        val event = PaymentFailedEvent(
            eventId = UUID.randomUUID(),
            paymentId = payment.id,
            ticketId = payment.ticketId,
            userId = payment.userId,
            sourceEventId = payment.eventId,
            amount = payment.amount,
            currency = payment.currency,
            reason = payment.failureReason ?: "Payment failed",
            occurredAt = Instant.now(),
        )
        return paymentEvent(payment, event.eventType, topics.paymentFailed, objectMapper.writeValueAsString(event))
    }

    private fun paymentEvent(payment: Payment, eventType: String, topic: String, payload: String): OutboxEvent =
        OutboxEvent(
            aggregateType = "payment",
            aggregateId = payment.id,
            eventType = eventType,
            topic = topic,
            messageKey = payment.id.toString(),
            payload = payload,
        )
}
