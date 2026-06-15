package com.ems.ticketservice.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class CreateTicketRequest(
    @field:NotNull(message = "userId must not be null")
    val userId: UUID?,

    @field:NotNull(message = "eventId must not be null")
    val eventId: UUID?,

    @field:NotBlank(message = "holderName must not be blank")
    val holderName: String,

    @field:NotBlank(message = "seatCode must not be blank")
    val seatCode: String,
)
