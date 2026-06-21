package com.ems.notificationservice.repository

import com.ems.notificationservice.domain.Notification
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, UUID> {
    fun findAllByRecipientUserIdOrderByCreatedAtDesc(recipientUserId: UUID): List<Notification>
}
