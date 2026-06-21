package com.ems.notificationservice.service

import com.ems.notificationservice.domain.Notification
import com.ems.notificationservice.domain.NotificationChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

fun interface NotificationSender {
    fun send(notification: Notification)
}

@Component
class RoutingNotificationSender(
    private val senders: List<ChannelNotificationSender>,
) : NotificationSender {
    override fun send(notification: Notification) {
        val sender = senders.firstOrNull { sender -> sender.supports(notification.channel) }
            ?: error("No notification sender registered for channel ${notification.channel}")
        sender.send(notification)
    }
}

interface ChannelNotificationSender {
    fun supports(channel: NotificationChannel): Boolean

    fun send(notification: Notification)
}

@Component
class EmailNotificationSender : ChannelNotificationSender {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun supports(channel: NotificationChannel): Boolean = channel == NotificationChannel.EMAIL

    override fun send(notification: Notification) {
        log.info(
            "Sending email notification {} to user {}: {}",
            notification.id,
            notification.recipientUserId,
            notification.subject,
        )
    }
}

@Component
class PushNotificationSender : ChannelNotificationSender {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun supports(channel: NotificationChannel): Boolean = channel == NotificationChannel.PUSH

    override fun send(notification: Notification) {
        log.info(
            "Sending push notification {} to user {}: {}",
            notification.id,
            notification.recipientUserId,
            notification.subject,
        )
    }
}
