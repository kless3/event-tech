package com.ems.ticketservice.repository

import com.ems.ticketservice.domain.OutboxEvent
import com.ems.ticketservice.domain.OutboxEventStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OutboxEventRepository : JpaRepository<OutboxEvent, UUID> {
    fun findAllByStatusOrderByCreatedAtAsc(status: OutboxEventStatus, pageable: Pageable): List<OutboxEvent>
}
