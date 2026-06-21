package com.ems.ticketservice.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.math.BigDecimal
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

    @field:NotNull(message = "amount must not be null")
    @field:DecimalMin(value = "0.01", message = "amount must be greater than zero")
    val amount: BigDecimal?,

    @field:NotBlank(message = "currency must not be blank")
    @field:Pattern(regexp = "^[A-Z]{3}$", message = "currency must be an ISO 4217 code")
    val currency: String,
)
