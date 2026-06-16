package com.ems.eventservice.repository

import com.ems.eventservice.domain.OutboxEvent
import com.ems.eventservice.domain.OutboxEventStatus
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxEventRepository : JpaRepository<OutboxEvent, UUID> {
    fun findAllByStatusOrderByCreatedAtAsc(status: OutboxEventStatus, pageable: Pageable): List<OutboxEvent>
}
