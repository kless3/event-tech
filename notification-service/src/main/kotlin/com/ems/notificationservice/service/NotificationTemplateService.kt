package com.ems.notificationservice.service

import com.ems.notificationservice.domain.NotificationChannel
import com.ems.notificationservice.dto.event.EventCancelledEvent
import com.ems.notificationservice.dto.event.PaymentFailedEvent
import com.ems.notificationservice.dto.event.PaymentSucceededEvent
import org.springframework.stereotype.Service

@Service
class NotificationTemplateService {
    fun paymentSucceeded(event: PaymentSucceededEvent): NotificationMessage =
        NotificationMessage(
            channel = NotificationChannel.EMAIL,
            subject = "Payment confirmed",
            body = "Payment ${event.paymentId} for ticket ${event.ticketId} was completed. " +
                "Amount: ${event.amount} ${event.currency}. Receipt: ${event.receiptUrl}",
        )

    fun paymentFailed(event: PaymentFailedEvent): NotificationMessage =
        NotificationMessage(
            channel = NotificationChannel.EMAIL,
            subject = "Payment failed",
            body = "Payment ${event.paymentId} for ticket ${event.ticketId} failed. " +
                "Amount: ${event.amount} ${event.currency}. Reason: ${event.reason}",
        )

    fun eventCancelled(event: EventCancelledEvent): NotificationMessage =
        NotificationMessage(
            channel = NotificationChannel.PUSH,
            subject = "Event cancelled",
            body = "Event ${event.cancelledEventId} was cancelled. Reason: ${event.reason}",
        )
}

data class NotificationMessage(
    val channel: NotificationChannel,
    val subject: String,
    val body: String,
)
