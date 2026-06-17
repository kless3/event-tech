package com.ems.userservice.domain

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
@Table(name = "outbox_events")
class OutboxEvent(
    @Id
    @Column(nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "aggregate_type", nullable = false, length = 64)
    val aggregateType: String,

    @Column(name = "aggregate_id", nullable = false)
    val aggregateId: UUID,

    @Column(name = "event_type", nullable = false, length = 128)
    val eventType: String,

    @Column(nullable = false)
    val topic: String,

    @Column(name = "message_key", nullable = false)
    val messageKey: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32, columnDefinition = "VARCHAR(32)")
    var status: OutboxEventStatus = OutboxEventStatus.PENDING,

    @Column(nullable = false)
    var attempts: Int = 0,

    @Column(name = "last_error", length = 1024)
    var lastError: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,

    @Column(name = "published_at")
    var publishedAt: LocalDateTime? = null,
) {
    fun markPublished(publishedAt: LocalDateTime) {
        status = OutboxEventStatus.PUBLISHED
        this.publishedAt = publishedAt
        lastError = null
    }

    fun markPublishFailed(error: String, maxAttempts: Int) {
        attempts += 1
        status = if (attempts >= maxAttempts) OutboxEventStatus.FAILED else OutboxEventStatus.PENDING
        lastError = error.take(MAX_ERROR_LENGTH)
    }

    private companion object {
        const val MAX_ERROR_LENGTH = 1024
    }
}
