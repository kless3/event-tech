package com.ems.eventservice.domain

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
@Table(name = "events")
class Event(
    @Id
    @Column(nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),

    @Column(name = "organizer_user_id")
    var organizerUserId: UUID?,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String?,

    @Column(nullable = false)
    var location: String,

    @Column(name = "starts_at", nullable = false)
    var startsAt: LocalDateTime,

    @Column(nullable = false)
    var capacity: Int,

    @Column(name = "tickets_sold", nullable = false)
    var ticketsSold: Int = 0,

    @Column(name = "encrypted_organizer_note", columnDefinition = "VARCHAR")
    var encryptedOrganizerNote: String?,

    @Column(name = "organizer_note_iv", columnDefinition = "VARCHAR")
    var organizerNoteIv: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    var status: EventStatus = EventStatus.PUBLISHED,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null,

    @Column(name = "cancelled_at")
    var cancelledAt: LocalDateTime? = null,

    @Column(name = "gdpr_erased_at")
    var gdprErasedAt: LocalDateTime? = null,
) {
    fun incrementTicketsSold() {
        ticketsSold += 1
    }

    fun releaseTicketReservation() {
        if (ticketsSold > 0) {
            ticketsSold -= 1
        }
    }

    fun cancel(cancelledAt: LocalDateTime) {
        status = EventStatus.CANCELLED
        this.cancelledAt = cancelledAt
    }

    fun eraseOrganizer(erasedAt: LocalDateTime) {
        organizerUserId = null
        encryptedOrganizerNote = null
        organizerNoteIv = null
        gdprErasedAt = erasedAt
        if (status == EventStatus.PUBLISHED || status == EventStatus.DRAFT) {
            status = EventStatus.ORGANIZER_ERASED
            cancelledAt = erasedAt
        }
    }
}
