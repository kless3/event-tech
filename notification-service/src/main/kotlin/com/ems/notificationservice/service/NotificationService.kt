package com.ems.notificationservice.service

import com.ems.notificationservice.domain.Notification
import com.ems.notificationservice.domain.NotificationChannel
import com.ems.notificationservice.dto.event.EventCancelledEvent
import com.ems.notificationservice.dto.event.PaymentFailedEvent
import com.ems.notificationservice.dto.event.PaymentSucceededEvent
import com.ems.notificationservice.dto.response.NotificationResponse
import com.ems.notificationservice.exception.NotificationNotFoundException
import com.ems.notificationservice.repository.NotificationRepository
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val notificationSender: NotificationSender,
) {
    @Transactional
    fun notifyPaymentSucceeded(event: PaymentSucceededEvent): NotificationResponse =
        createAndSend(
            recipientUserId = event.userId,
            sourceEventId = event.eventId,
            sourceEventType = event.eventType,
            subject = "Payment succeeded",
            body = "Your payment ${event.paymentId} for ticket ${event.ticketId} was completed. Receipt: ${event.receiptUrl}",
        )

    @Transactional
    fun notifyPaymentFailed(event: PaymentFailedEvent): NotificationResponse =
        createAndSend(
            recipientUserId = event.userId,
            sourceEventId = event.eventId,
            sourceEventType = event.eventType,
            subject = "Payment failed",
            body = "Your payment ${event.paymentId} for ticket ${event.ticketId} failed: ${event.reason}",
        )

    @Transactional
    fun notifyEventCancelled(event: EventCancelledEvent): NotificationResponse =
        createAndSend(
            recipientUserId = null,
            sourceEventId = event.eventId,
            sourceEventType = event.eventType,
            subject = "Event cancelled",
            body = "Event ${event.cancelledEventId} was cancelled: ${event.reason}",
            channel = NotificationChannel.PUSH,
        )

    @Transactional(readOnly = true)
    fun getNotification(id: UUID): NotificationResponse =
        notificationRepository.findById(id)
            .orElseThrow { NotificationNotFoundException(id) }
            .toResponse()

    @Transactional(readOnly = true)
    fun getNotificationsForUser(userId: UUID): List<NotificationResponse> =
        notificationRepository.findAllByRecipientUserIdOrderByCreatedAtDesc(userId).map(Notification::toResponse)

    private fun createAndSend(
        recipientUserId: UUID?,
        sourceEventId: UUID,
        sourceEventType: String,
        subject: String,
        body: String,
        channel: NotificationChannel = NotificationChannel.EMAIL,
    ): NotificationResponse {
        val notification = notificationRepository.save(
            Notification(
                recipientUserId = recipientUserId,
                channel = channel,
                sourceEventId = sourceEventId,
                sourceEventType = sourceEventType,
                subject = subject,
                body = body,
            ),
        )

        try {
            notificationSender.send(notification)
            notification.markSent(LocalDateTime.now())
        } catch (exception: Exception) {
            notification.markFailed(exception.message ?: "Notification delivery failed")
        }

        return notification.toResponse()
    }
}
