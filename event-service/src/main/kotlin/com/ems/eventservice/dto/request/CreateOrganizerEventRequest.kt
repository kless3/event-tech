package com.ems.eventservice.dto.request

import jakarta.validation.constraints.Future
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class CreateOrganizerEventRequest(
    @field:NotBlank(message = "title must not be blank")
    val title: String,

    val description: String?,

    @field:NotBlank(message = "location must not be blank")
    val location: String,

    @field:NotNull(message = "startsAt must not be null")
    @field:Future(message = "startsAt must be in the future")
    val startsAt: LocalDateTime?,

    @field:Min(value = 1, message = "capacity must be greater than zero")
    val capacity: Int,

    val organizerNote: String?,
)
