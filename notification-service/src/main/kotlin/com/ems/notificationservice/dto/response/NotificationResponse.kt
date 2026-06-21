package com.ems.notificationservice.dto.response

import com.ems.notificationservice.domain.NotificationChannel
import com.ems.notificationservice.domain.NotificationStatus
import java.time.LocalDateTime
import java.util.UUID

data class NotificationResponse(
    val id: UUID,
    val recipientUserId: UUID?,
    val channel: NotificationChannel,
    val status: NotificationStatus,
    val sourceEventId: UUID,
    val sourceEventType: String,
    val subject: String,
    val body: String,
    val failureReason: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val sentAt: LocalDateTime?,
)
