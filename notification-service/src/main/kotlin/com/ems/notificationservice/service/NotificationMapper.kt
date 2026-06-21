package com.ems.notificationservice.service

import com.ems.notificationservice.domain.Notification
import com.ems.notificationservice.dto.response.NotificationResponse

fun Notification.toResponse(): NotificationResponse =
    NotificationResponse(
        id = id,
        recipientUserId = recipientUserId,
        channel = channel,
        status = status,
        sourceEventId = sourceEventId,
        sourceEventType = sourceEventType,
        subject = subject,
        body = body,
        failureReason = failureReason,
        createdAt = createdAt,
        updatedAt = updatedAt,
        sentAt = sentAt,
    )
