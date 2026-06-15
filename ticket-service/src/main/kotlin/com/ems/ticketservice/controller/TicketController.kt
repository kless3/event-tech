package com.ems.ticketservice.controller

import com.ems.ticketservice.dto.request.CreateTicketRequest
import com.ems.ticketservice.dto.response.TicketResponse
import com.ems.ticketservice.service.TicketService
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
@RequestMapping("/api/v1/tickets")
class TicketController(
    private val ticketService: TicketService,
) {
    @PostMapping
    fun createTicket(@Valid @RequestBody request: CreateTicketRequest): ResponseEntity<TicketResponse> {
        val response = ticketService.createTicket(request)
        return ResponseEntity
            .created(URI.create("/api/v1/tickets/${response.id}"))
            .body(response)
    }

    @GetMapping("/{id}")
    fun getTicket(@PathVariable id: UUID): TicketResponse =
        ticketService.getTicket(id)

    @DeleteMapping("/{id}")
    fun cancelTicket(@PathVariable id: UUID): TicketResponse =
        ticketService.cancelTicket(id)
}
