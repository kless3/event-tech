package com.ems.paymentservice.dto.request

import jakarta.validation.constraints.NotBlank

data class FailPaymentRequest(
    @field:NotBlank
    val reason: String,
)
