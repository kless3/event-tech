package com.ems.notificationservice.service

import com.ems.notificationservice.config.NotificationDeliveryProperties
import com.ems.notificationservice.domain.Notification
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
    private val templateService: NotificationTemplateService,
    private val deliveryProperties: NotificationDeliveryProperties,
) {
    @Transactional
    fun notifyPaymentSucceeded(event: PaymentSucceededEvent): NotificationResponse =
        createAndSend(
            recipientUserId = event.userId,
            sourceEventId = event.eventId,
            sourceEventType = event.eventType,
            message = templateService.paymentSucceeded(event),
        )

    @Transactional
    fun notifyPaymentFailed(event: PaymentFailedEvent): NotificationResponse =
        createAndSend(
            recipientUserId = event.userId,
            sourceEventId = event.eventId,
            sourceEventType = event.eventType,
            message = templateService.paymentFailed(event),
        )

    @Transactional
    fun notifyEventCancelled(event: EventCancelledEvent): NotificationResponse =
        createAndSend(
            recipientUserId = null,
            sourceEventId = event.eventId,
            sourceEventType = event.eventType,
            message = templateService.eventCancelled(event),
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
        message: NotificationMessage,
    ): NotificationResponse {
        val notification = notificationRepository.save(
            Notification(
                recipientUserId = recipientUserId,
                channel = message.channel,
                sourceEventId = sourceEventId,
                sourceEventType = sourceEventType,
                subject = message.subject,
                body = message.body,
            ),
        )

        sendWithRetry(notification)

        return notification.toResponse()
    }

    private fun sendWithRetry(notification: Notification) {
        var lastFailure: Exception? = null
        for (attempt in 1..deliveryProperties.maxAttempts.coerceAtLeast(1)) {
            notification.markDeliveryAttempt(attempt)
            try {
                notificationSender.send(notification)
                notification.markSent(LocalDateTime.now())
                return
            } catch (exception: Exception) {
                lastFailure = exception
            }
        }
        notification.markFailed(lastFailure?.message ?: "Notification delivery failed")
    }
}
