package com.ems.paymentservice.repository

import com.ems.paymentservice.domain.Payment
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, UUID> {
    fun findByIdempotencyKey(idempotencyKey: String): Optional<Payment>
    fun findByTicketId(ticketId: UUID): Optional<Payment>
}
