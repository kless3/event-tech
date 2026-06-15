package com.ems.userservice.repository

import com.ems.userservice.domain.OutboxEvent
import com.ems.userservice.domain.OutboxEventStatus
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OutboxEventRepository : JpaRepository<OutboxEvent, UUID> {
    fun findAllByStatusOrderByCreatedAtAsc(status: OutboxEventStatus, pageable: Pageable): List<OutboxEvent>
}
