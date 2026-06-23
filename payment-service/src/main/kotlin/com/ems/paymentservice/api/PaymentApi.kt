package com.ems.paymentservice.api

import com.ems.paymentservice.dto.request.CreatePaymentRequest
import com.ems.paymentservice.dto.request.FailPaymentRequest
import com.ems.paymentservice.dto.response.PaymentReceiptResponse
import com.ems.paymentservice.dto.response.PaymentResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.ResponseEntity

@Tag(name = "Payments", description = "Payment lifecycle and receipt endpoints")
interface PaymentApi {
    @Operation(summary = "Create a payment")
    @ApiResponse(responseCode = "201", description = "Payment created")
    @ApiResponse(responseCode = "400", description = "Invalid payment payload")
    suspend fun createPayment(@Valid request: CreatePaymentRequest): ResponseEntity<PaymentResponse>

    @Operation(summary = "Get payment details")
    @ApiResponse(responseCode = "200", description = "Payment found")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    suspend fun getPayment(@Parameter(description = "Payment id") id: UUID): PaymentResponse

    @Operation(summary = "Get payment by ticket id")
    @ApiResponse(responseCode = "200", description = "Payment found")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    suspend fun getPaymentByTicketId(
        @Parameter(description = "Ticket id") ticketId: UUID,
    ): PaymentResponse

    @Operation(summary = "Get payment receipt")
    @ApiResponse(responseCode = "200", description = "Receipt found")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    suspend fun getReceipt(@Parameter(description = "Payment id") id: UUID): PaymentReceiptResponse

    @Operation(summary = "Capture a payment")
    @ApiResponse(responseCode = "200", description = "Payment captured")
    @ApiResponse(responseCode = "409", description = "Payment cannot be captured")
    suspend fun capturePayment(@Parameter(description = "Payment id") id: UUID): PaymentResponse

    @Operation(summary = "Fail a payment")
    @ApiResponse(responseCode = "200", description = "Payment failed")
    @ApiResponse(responseCode = "409", description = "Payment cannot be failed")
    suspend fun failPayment(
        @Parameter(description = "Payment id") id: UUID,
        @Valid request: FailPaymentRequest,
    ): PaymentResponse
}
