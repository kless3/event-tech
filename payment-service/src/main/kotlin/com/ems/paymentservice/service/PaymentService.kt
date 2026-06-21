package com.ems.paymentservice.service

import com.ems.paymentservice.domain.Payment
import com.ems.paymentservice.domain.PaymentStatus
import com.ems.paymentservice.dto.event.TicketCreatedEvent
import com.ems.paymentservice.dto.request.CreatePaymentRequest
import com.ems.paymentservice.dto.response.PaymentReceiptResponse
import com.ems.paymentservice.dto.response.PaymentResponse
import com.ems.paymentservice.exception.PaymentNotFoundException
import com.ems.paymentservice.exception.PaymentReceiptUnavailableException
import com.ems.paymentservice.exception.PaymentStateException
import com.ems.paymentservice.mapper.toResponse
import com.ems.paymentservice.messaging.OutboxEventFactory
import com.ems.paymentservice.receipt.PaymentReceipt
import com.ems.paymentservice.receipt.PdfReceiptGenerator
import com.ems.paymentservice.receipt.ReceiptStorage
import com.ems.paymentservice.repository.OutboxEventRepository
import com.ems.paymentservice.repository.PaymentRepository
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val outboxEventRepository: OutboxEventRepository,
    private val outboxEventFactory: OutboxEventFactory,
    private val pdfReceiptGenerator: PdfReceiptGenerator,
    private val receiptStorage: ReceiptStorage,
) {
    @Transactional
    fun createPayment(request: CreatePaymentRequest): PaymentResponse {
        val idempotencyKey = request.idempotencyKey.trim()
        val existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey).orElse(null)
        if (existingPayment != null) {
            return existingPayment.toResponse()
        }

        val ticketId = requireNotNull(request.ticketId) { "ticketId must not be null" }
        val existingTicketPayment = paymentRepository.findByTicketId(ticketId).orElse(null)
        if (existingTicketPayment != null) {
            return existingTicketPayment.toResponse()
        }

        val payment = paymentRepository.save(
            Payment(
                ticketId = ticketId,
                userId = requireNotNull(request.userId) { "userId must not be null" },
                eventId = requireNotNull(request.eventId) { "eventId must not be null" },
                amount = requireNotNull(request.amount) { "amount must not be null" }.setScale(2, RoundingMode.HALF_UP),
                currency = request.currency.trim().uppercase(),
                idempotencyKey = idempotencyKey,
            ),
        )
        outboxEventRepository.save(outboxEventFactory.paymentCreated(payment))
        return payment.toResponse()
    }

    @Transactional
    fun createPaymentForTicket(event: TicketCreatedEvent): PaymentResponse =
        createPayment(
            CreatePaymentRequest(
                ticketId = event.ticketId,
                userId = event.userId,
                eventId = event.eventIdRef,
                amount = event.amount,
                currency = event.currency,
                idempotencyKey = "ticket:${event.ticketId}",
            ),
        )

    @Transactional(readOnly = true)
    fun getPayment(id: UUID): PaymentResponse =
        findPayment(id).toResponse()

    @Transactional(readOnly = true)
    fun getPaymentByTicketId(ticketId: UUID): PaymentResponse =
        paymentRepository.findByTicketId(ticketId)
            .orElseThrow { PaymentNotFoundException(ticketId) }
            .toResponse()

    @Transactional(readOnly = true)
    fun getReceipt(id: UUID): PaymentReceiptResponse {
        val payment = findPayment(id)
        val receiptObjectKey = payment.receiptObjectKey ?: throw PaymentReceiptUnavailableException(id)
        val receiptUrl = payment.receiptUrl ?: throw PaymentReceiptUnavailableException(id)
        val paidAt = payment.paidAt ?: throw PaymentReceiptUnavailableException(id)
        return PaymentReceiptResponse(
            paymentId = payment.id,
            ticketId = payment.ticketId,
            receiptObjectKey = receiptObjectKey,
            receiptUrl = receiptUrl,
            paidAt = paidAt,
        )
    }

    @Transactional
    fun capturePayment(id: UUID): PaymentResponse {
        val payment = findPayment(id)
        if (payment.status == PaymentStatus.SUCCEEDED) {
            return payment.toResponse()
        }
        ensurePending(payment)

        val paidAt = LocalDateTime.now()
        val receipt = PaymentReceipt(
            paymentId = payment.id,
            ticketId = payment.ticketId,
            userId = payment.userId,
            eventId = payment.eventId,
            amount = payment.amount,
            currency = payment.currency,
            paidAt = paidAt,
        )
        val objectKey = "receipts/${payment.id}.pdf"
        val storedReceipt = receiptStorage.store(objectKey, pdfReceiptGenerator.generate(receipt))

        payment.markSucceeded(
            receiptObjectKey = storedReceipt.objectKey,
            receiptUrl = storedReceipt.url,
            paidAt = paidAt,
        )
        outboxEventRepository.save(outboxEventFactory.paymentSucceeded(payment))
        return payment.toResponse()
    }

    @Transactional
    fun failPayment(id: UUID, reason: String): PaymentResponse {
        val payment = findPayment(id)
        ensurePending(payment)

        payment.markFailed(reason.trim())
        outboxEventRepository.save(outboxEventFactory.paymentFailed(payment))
        return payment.toResponse()
    }

    private fun findPayment(id: UUID): Payment =
        paymentRepository.findById(id).orElseThrow { PaymentNotFoundException(id) }

    private fun ensurePending(payment: Payment) {
        if (payment.status != PaymentStatus.PENDING) {
            throw PaymentStateException(payment.id, payment.status)
        }
    }
}
