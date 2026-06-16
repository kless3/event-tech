package com.ems.ticketservice.repository

import com.ems.ticketservice.domain.Ticket
import com.ems.ticketservice.domain.TicketStatus
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface TicketRepository : JpaRepository<Ticket, UUID> {
    fun findAllByUserIdAndStatus(userId: UUID, status: TicketStatus): List<Ticket>
    fun findAllByEventIdAndStatus(eventId: UUID, status: TicketStatus): List<Ticket>
    fun countByEventIdAndStatus(eventId: UUID, status: TicketStatus): Long
}
