package com.ems.paymentservice.dto.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.math.BigDecimal
import java.util.UUID

data class CreatePaymentRequest(
    @field:NotNull
    val ticketId: UUID?,

    @field:NotNull
    val userId: UUID?,

    @field:NotNull
    val eventId: UUID?,

    @field:NotNull
    @field:DecimalMin(value = "0.01")
    val amount: BigDecimal?,

    @field:NotBlank
    @field:Pattern(regexp = "^[A-Z]{3}$")
    val currency: String,

    @field:NotBlank
    val idempotencyKey: String,
)
