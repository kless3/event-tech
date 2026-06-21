package com.ems.notificationservice.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@Entity
@Table(name = "notifications")
class Notification(
    @Id
    @Column(nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "recipient_user_id", updatable = false)
    val recipientUserId: UUID?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    val channel: NotificationChannel,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: NotificationStatus = NotificationStatus.PENDING,

    @Column(name = "source_event_id", nullable = false, updatable = false)
    val sourceEventId: UUID,

    @Column(name = "source_event_type", nullable = false, updatable = false, length = 128)
    val sourceEventType: String,

    @Column(nullable = false, length = 255)
    val subject: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val body: String,

    @Column(name = "failure_reason", length = 1024)
    var failureReason: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,

    @Column(name = "sent_at")
    var sentAt: LocalDateTime? = null,
) {
    fun markSent(sentAt: LocalDateTime) {
        status = NotificationStatus.SENT
        failureReason = null
        this.sentAt = sentAt
    }

    fun markFailed(reason: String) {
        status = NotificationStatus.FAILED
        failureReason = reason.take(MAX_FAILURE_REASON_LENGTH)
    }

    private companion object {
        const val MAX_FAILURE_REASON_LENGTH = 1024
    }
}
