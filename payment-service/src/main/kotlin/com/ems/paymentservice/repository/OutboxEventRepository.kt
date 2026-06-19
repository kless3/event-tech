package com.ems.paymentservice.repository

import com.ems.paymentservice.domain.OutboxEvent
import com.ems.paymentservice.domain.OutboxEventStatus
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxEventRepository : JpaRepository<OutboxEvent, UUID> {
    fun findAllByStatusOrderByCreatedAtAsc(status: OutboxEventStatus, pageable: Pageable): List<OutboxEvent>
}
