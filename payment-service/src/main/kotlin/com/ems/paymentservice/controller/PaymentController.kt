package com.ems.paymentservice.controller

import com.ems.paymentservice.dto.request.CreatePaymentRequest
import com.ems.paymentservice.dto.request.FailPaymentRequest
import com.ems.paymentservice.dto.response.PaymentReceiptResponse
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
    suspend fun createPayment(@Valid @RequestBody request: CreatePaymentRequest): ResponseEntity<PaymentResponse> =
        blockingEndpoint {
            val response = paymentService.createPayment(request)
            ResponseEntity
                .created(URI.create("/api/v1/payments/${response.id}"))
                .body(response)
        }

    @GetMapping("/{id}")
    suspend fun getPayment(@PathVariable id: UUID): PaymentResponse =
        blockingEndpoint { paymentService.getPayment(id) }

    @GetMapping("/by-ticket/{ticketId}")
    suspend fun getPaymentByTicketId(@PathVariable ticketId: UUID): PaymentResponse =
        blockingEndpoint { paymentService.getPaymentByTicketId(ticketId) }

    @GetMapping("/{id}/receipt")
    suspend fun getReceipt(@PathVariable id: UUID): PaymentReceiptResponse =
        blockingEndpoint { paymentService.getReceipt(id) }

    @PostMapping("/{id}/capture")
    suspend fun capturePayment(@PathVariable id: UUID): PaymentResponse =
        blockingEndpoint { paymentService.capturePayment(id) }

    @PostMapping("/{id}/fail")
    suspend fun failPayment(
        @PathVariable id: UUID,
        @Valid @RequestBody request: FailPaymentRequest,
    ): PaymentResponse =
        blockingEndpoint { paymentService.failPayment(id, request.reason) }
}
