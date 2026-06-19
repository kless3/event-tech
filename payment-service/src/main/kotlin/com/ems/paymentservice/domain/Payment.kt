package com.ems.paymentservice.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @Column(nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "ticket_id", nullable = false, updatable = false)
    val ticketId: UUID,

    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UUID,

    @Column(name = "event_id", nullable = false, updatable = false)
    val eventId: UUID,

    @Column(nullable = false, precision = 19, scale = 2)
    val amount: BigDecimal,

    @Column(nullable = false, length = 3)
    val currency: String,

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    val idempotencyKey: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: PaymentStatus = PaymentStatus.PENDING,

    @Column(name = "failure_reason", length = 1024)
    var failureReason: String? = null,

    @Column(name = "receipt_object_key", length = 512)
    var receiptObjectKey: String? = null,

    @Column(name = "receipt_url", length = 1024)
    var receiptUrl: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,

    @Column(name = "paid_at")
    var paidAt: LocalDateTime? = null,
) {
    fun markSucceeded(receiptObjectKey: String, receiptUrl: String, paidAt: LocalDateTime) {
        status = PaymentStatus.SUCCEEDED
        failureReason = null
        this.receiptObjectKey = receiptObjectKey
        this.receiptUrl = receiptUrl
        this.paidAt = paidAt
    }

    fun markFailed(reason: String) {
        status = PaymentStatus.FAILED
        failureReason = reason.take(MAX_FAILURE_REASON_LENGTH)
    }

    private companion object {
        const val MAX_FAILURE_REASON_LENGTH = 1024
    }
}
