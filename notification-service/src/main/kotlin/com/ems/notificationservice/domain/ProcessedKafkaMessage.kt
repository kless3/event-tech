package com.ems.notificationservice.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "processed_kafka_messages")
class ProcessedKafkaMessage(
    @Id
    @Column(name = "message_id", nullable = false, updatable = false)
    val messageId: UUID,

    @Column(nullable = false)
    val topic: String,

    @Column(name = "message_key", nullable = false)
    val messageKey: String,

    @Column(name = "processed_at", nullable = false)
    val processedAt: LocalDateTime,
)
