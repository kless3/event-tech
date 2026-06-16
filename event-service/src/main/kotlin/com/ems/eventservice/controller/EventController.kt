package com.ems.eventservice.controller

import com.ems.eventservice.dto.request.CreateEventRequest
import com.ems.eventservice.dto.response.EventAvailabilityResponse
import com.ems.eventservice.dto.response.EventResponse
import com.ems.eventservice.service.EventService
import jakarta.validation.Valid
import java.net.URI
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/events")
class EventController(
    private val eventService: EventService,
) {
    @PostMapping
    fun createEvent(@Valid @RequestBody request: CreateEventRequest): ResponseEntity<EventResponse> {
        val response = eventService.createEvent(request)
        return ResponseEntity
            .created(URI.create("/api/v1/events/${response.id}"))
            .body(response)
    }

    @GetMapping("/{id}")
    fun getEvent(@PathVariable id: UUID): EventResponse =
        eventService.getEvent(id)

    @GetMapping("/{id}/availability")
    fun getAvailability(@PathVariable id: UUID): EventAvailabilityResponse =
        eventService.getAvailability(id)

    @DeleteMapping("/{id}")
    fun cancelEvent(@PathVariable id: UUID): EventResponse =
        eventService.cancelEvent(id)
}
