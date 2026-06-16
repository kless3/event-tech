package com.ems.eventservice.repository

import com.ems.eventservice.domain.Event
import com.ems.eventservice.domain.EventStatus
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface EventRepository : JpaRepository<Event, UUID> {
    fun findAllByOrganizerUserIdAndStatusIn(organizerUserId: UUID, statuses: Collection<EventStatus>): List<Event>
}
