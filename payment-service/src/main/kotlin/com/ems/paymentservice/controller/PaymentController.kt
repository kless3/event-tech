package com.ems.paymentservice.controller

import com.ems.paymentservice.dto.request.CreatePaymentRequest
import com.ems.paymentservice.dto.request.FailPaymentRequest
import com.ems.paymentservice.dto.response.PaymentResponse
import com.ems.paymentservice.service.PaymentService
import jakarta.validation.Valid
import java.net.URI
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @PostMapping
    fun createPayment(@Valid @RequestBody request: CreatePaymentRequest): ResponseEntity<PaymentResponse> {
        val response = paymentService.createPayment(request)
        return ResponseEntity
            .created(URI.create("/api/v1/payments/${response.id}"))
            .body(response)
    }

    @GetMapping("/{id}")
    fun getPayment(@PathVariable id: UUID): PaymentResponse =
        paymentService.getPayment(id)

    @GetMapping("/by-ticket/{ticketId}")
    fun getPaymentByTicketId(@PathVariable ticketId: UUID): PaymentResponse =
        paymentService.getPaymentByTicketId(ticketId)

    @PostMapping("/{id}/capture")
    fun capturePayment(@PathVariable id: UUID): PaymentResponse =
        paymentService.capturePayment(id)

    @PostMapping("/{id}/fail")
    fun failPayment(
        @PathVariable id: UUID,
        @Valid @RequestBody request: FailPaymentRequest,
    ): PaymentResponse =
        paymentService.failPayment(id, request.reason)
}
