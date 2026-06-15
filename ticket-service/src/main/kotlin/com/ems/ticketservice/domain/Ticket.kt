package com.ems.ticketservice.domain

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
@Table(name = "tickets")
class Ticket(
    @Id
    @Column(nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id")
    var userId: UUID?,

    @Column(name = "event_id", nullable = false, updatable = false)
    val eventId: UUID,

    @Column(name = "encrypted_payload", columnDefinition = "VARCHAR")
    var encryptedPayload: String?,

    @Column(name = "payload_iv", columnDefinition = "VARCHAR")
    var payloadIv: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: TicketStatus = TicketStatus.ACTIVE,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,

    @Column(name = "gdpr_erased_at")
    var gdprErasedAt: LocalDateTime? = null,
) {
    fun eraseForGdpr(erasedAt: LocalDateTime) {
        userId = null
        encryptedPayload = null
        payloadIv = null
        status = TicketStatus.USER_ERASED
        gdprErasedAt = erasedAt
    }

    fun cancel() {
        status = TicketStatus.CANCELLED
    }
}
