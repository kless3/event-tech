package com.ems.notificationservice.service

import com.ems.notificationservice.domain.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

fun interface NotificationSender {
    fun send(notification: Notification)
}

@Component
class LoggingNotificationSender : NotificationSender {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(notification: Notification) {
        log.info(
            "Sending {} notification {} to user {}: {}",
            notification.channel,
            notification.id,
            notification.recipientUserId,
            notification.subject,
        )
    }
}
