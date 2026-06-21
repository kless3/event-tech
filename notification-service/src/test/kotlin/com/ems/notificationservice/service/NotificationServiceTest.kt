package com.ems.notificationservice.service

import com.ems.notificationservice.config.NotificationDeliveryProperties
import com.ems.notificationservice.domain.Notification
import com.ems.notificationservice.domain.NotificationChannel
import com.ems.notificationservice.domain.NotificationStatus
import com.ems.notificationservice.dto.event.EventCancelledEvent
import com.ems.notificationservice.dto.event.PaymentFailedEvent
import com.ems.notificationservice.dto.event.PaymentSucceededEvent
import com.ems.notificationservice.repository.NotificationRepository
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.mockito.Mockito

class NotificationServiceTest {
    private val notificationRepository = Mockito.mock(NotificationRepository::class.java)
    private val notificationSender = RecordingNotificationSender()
    private val notificationService = NotificationService(
        notificationRepository,
        notificationSender,
        NotificationTemplateService(),
        NotificationDeliveryProperties(maxAttempts = 3),
    )

    @Test
    fun `creates sent notification for successful payment`() {
        Mockito.`when`(notificationRepository.save(Mockito.any(Notification::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Notification>(0)
        }
        val event = paymentSucceededEvent()

        val response = notificationService.notifyPaymentSucceeded(event)

        assertEquals(event.userId, response.recipientUserId)
        assertEquals(NotificationChannel.EMAIL, response.channel)
        assertEquals(NotificationStatus.SENT, response.status)
        assertEquals("payment.succeeded", response.sourceEventType)
        assertEquals(1, response.deliveryAttempts)
        assertEquals(response.id, notificationSender.lastNotificationId)
    }

    @Test
    fun `marks notification failed when sender fails`() {
        Mockito.`when`(notificationRepository.save(Mockito.any(Notification::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Notification>(0)
        }
        notificationSender.failure = IllegalStateException("SMTP unavailable")

        val response = notificationService.notifyPaymentFailed(paymentFailedEvent())

        assertEquals(NotificationStatus.FAILED, response.status)
        assertEquals("SMTP unavailable", response.failureReason)
        assertEquals(3, response.deliveryAttempts)
        assertEquals(3, notificationSender.deliveryAttempts)
    }

    @Test
    fun `creates system push notification for cancelled event`() {
        Mockito.`when`(notificationRepository.save(Mockito.any(Notification::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Notification>(0)
        }

        val response = notificationService.notifyEventCancelled(eventCancelledEvent())

        assertNull(response.recipientUserId)
        assertEquals(NotificationChannel.PUSH, response.channel)
        assertEquals(NotificationStatus.SENT, response.status)
        assertEquals(1, response.deliveryAttempts)
    }

    @Test
    fun `returns notifications for user`() {
        val userId = UUID.randomUUID()
        val notification = Notification(
            recipientUserId = userId,
            channel = NotificationChannel.EMAIL,
            sourceEventId = UUID.randomUUID(),
            sourceEventType = "payment.succeeded",
            subject = "Payment succeeded",
            body = "Done",
        )
        Mockito.`when`(notificationRepository.findAllByRecipientUserIdOrderByCreatedAtDesc(userId))
            .thenReturn(listOf(notification))

        val response = notificationService.getNotificationsForUser(userId)

        assertEquals(1, response.size)
        assertEquals(userId, response.first().recipientUserId)
    }

    @Test
    fun `returns notification by id`() {
        val notification = Notification(
            recipientUserId = UUID.randomUUID(),
            channel = NotificationChannel.EMAIL,
            sourceEventId = UUID.randomUUID(),
            sourceEventType = "payment.succeeded",
            subject = "Payment succeeded",
            body = "Done",
        )
        Mockito.`when`(notificationRepository.findById(notification.id)).thenReturn(Optional.of(notification))

        val response = notificationService.getNotification(notification.id)

        assertEquals(notification.id, response.id)
    }

    private fun paymentSucceededEvent(): PaymentSucceededEvent =
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

    private fun eventCancelledEvent(): EventCancelledEvent =
        EventCancelledEvent(
            eventId = UUID.randomUUID(),
            cancelledEventId = UUID.randomUUID(),
            reason = "organizer cancelled",
            occurredAt = Instant.now(),
        )

    private class RecordingNotificationSender : NotificationSender {
        var lastNotificationId: UUID? = null
            private set
        var deliveryAttempts: Int = 0
            private set
        var failure: RuntimeException? = null

        override fun send(notification: Notification) {
            deliveryAttempts++
            failure?.let { throw it }
            lastNotificationId = notification.id
        }
    }
}
