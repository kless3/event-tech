package com.ems.paymentservice.service

import com.ems.paymentservice.config.KafkaTopicsProperties
import com.ems.paymentservice.domain.OutboxEvent
import com.ems.paymentservice.domain.Payment
import com.ems.paymentservice.domain.PaymentStatus
import com.ems.paymentservice.dto.event.TicketCreatedEvent
import com.ems.paymentservice.dto.request.CreatePaymentRequest
import com.ems.paymentservice.exception.PaymentStateException
import com.ems.paymentservice.messaging.OutboxEventFactory
import com.ems.paymentservice.receipt.PdfReceiptGenerator
import com.ems.paymentservice.receipt.ReceiptStorage
import com.ems.paymentservice.receipt.StoredReceipt
import com.ems.paymentservice.repository.OutboxEventRepository
import com.ems.paymentservice.repository.PaymentRepository
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import tools.jackson.module.kotlin.jacksonMapperBuilder

class PaymentServiceTest {
    private val paymentRepository = Mockito.mock(PaymentRepository::class.java)
    private val outboxEventRepository = Mockito.mock(OutboxEventRepository::class.java)
    private val receiptStorage = RecordingReceiptStorage()
    private val objectMapper = jacksonMapperBuilder().build()
    private val outboxEventFactory = OutboxEventFactory(
        objectMapper = objectMapper,
        topics = KafkaTopicsProperties(
            ticketCreated = "ems.ticket.created",
            paymentCreated = "ems.payment.created",
            paymentSucceeded = "ems.payment.succeeded",
            paymentFailed = "ems.payment.failed",
            deadLetterSuffix = ".DLT",
        ),
    )
    private val paymentService = PaymentService(
        paymentRepository = paymentRepository,
        outboxEventRepository = outboxEventRepository,
        outboxEventFactory = outboxEventFactory,
        pdfReceiptGenerator = PdfReceiptGenerator(),
        receiptStorage = receiptStorage,
    )

    @Test
    fun `creates pending payment and outbox event`() {
        val request = createRequest()
        Mockito.`when`(paymentRepository.findByIdempotencyKey(request.idempotencyKey)).thenReturn(Optional.empty())
        Mockito.`when`(paymentRepository.findByTicketId(requireNotNull(request.ticketId))).thenReturn(Optional.empty())
        Mockito.`when`(paymentRepository.save(Mockito.any(Payment::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Payment>(0)
        }

        val response = paymentService.createPayment(request)

        assertEquals(request.ticketId, response.ticketId)
        assertEquals(request.userId, response.userId)
        assertEquals(request.eventId, response.eventId)
        assertEquals(BigDecimal("49.90"), response.amount)
        assertEquals("USD", response.currency)
        assertEquals(PaymentStatus.PENDING, response.status)

        val outboxCaptor = ArgumentCaptor.forClass(OutboxEvent::class.java)
        Mockito.verify(outboxEventRepository).save(outboxCaptor.capture())
        assertEquals("payment.created", outboxCaptor.value.eventType)
        assertEquals("ems.payment.created", outboxCaptor.value.topic)
    }

    @Test
    fun `returns existing payment for the same idempotency key`() {
        val request = createRequest()
        val existingPayment = paymentFrom(request)
        Mockito.`when`(paymentRepository.findByIdempotencyKey(request.idempotencyKey)).thenReturn(Optional.of(existingPayment))

        val response = paymentService.createPayment(request)

        assertEquals(existingPayment.id, response.id)
        Mockito.verify(paymentRepository, Mockito.never()).save(Mockito.any(Payment::class.java))
        Mockito.verify(outboxEventRepository, Mockito.never()).save(Mockito.any(OutboxEvent::class.java))
    }

    @Test
    fun `creates payment from ticket created event`() {
        val event = TicketCreatedEvent(
            eventId = UUID.randomUUID(),
            ticketId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            eventIdRef = UUID.randomUUID(),
            amount = BigDecimal("49.9"),
            currency = "USD",
            occurredAt = Instant.now(),
        )
        Mockito.`when`(paymentRepository.findByIdempotencyKey("ticket:${event.ticketId}")).thenReturn(Optional.empty())
        Mockito.`when`(paymentRepository.findByTicketId(event.ticketId)).thenReturn(Optional.empty())
        Mockito.`when`(paymentRepository.save(Mockito.any(Payment::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Payment>(0)
        }

        val response = paymentService.createPaymentForTicket(event)

        assertEquals(event.ticketId, response.ticketId)
        assertEquals(PaymentStatus.PENDING, response.status)
        Mockito.verify(outboxEventRepository).save(Mockito.any(OutboxEvent::class.java))
    }

    @Test
    fun `returns existing payment for the same ticket`() {
        val request = createRequest()
        val existingPayment = paymentFrom(request)
        Mockito.`when`(paymentRepository.findByIdempotencyKey(request.idempotencyKey)).thenReturn(Optional.empty())
        Mockito.`when`(paymentRepository.findByTicketId(requireNotNull(request.ticketId))).thenReturn(Optional.of(existingPayment))

        val response = paymentService.createPayment(request)

        assertEquals(existingPayment.id, response.id)
        Mockito.verify(paymentRepository, Mockito.never()).save(Mockito.any(Payment::class.java))
        Mockito.verify(outboxEventRepository, Mockito.never()).save(Mockito.any(OutboxEvent::class.java))
    }

    @Test
    fun `returns payment by ticket id`() {
        val payment = paymentFrom(createRequest())
        Mockito.`when`(paymentRepository.findByTicketId(payment.ticketId)).thenReturn(Optional.of(payment))

        val response = paymentService.getPaymentByTicketId(payment.ticketId)

        assertEquals(payment.id, response.id)
    }

    @Test
    fun `captures payment and stores pdf receipt`() {
        val payment = paymentFrom(createRequest())
        Mockito.`when`(paymentRepository.findById(payment.id)).thenReturn(Optional.of(payment))

        val response = paymentService.capturePayment(payment.id)

        assertEquals(PaymentStatus.SUCCEEDED, response.status)
        assertEquals("receipts/${payment.id}.pdf", response.receiptObjectKey)
        assertEquals("receipts/${payment.id}.pdf", receiptStorage.lastObjectKey)
        assertNotNull(receiptStorage.lastContent)
        assertNotNull(response.receiptUrl)
        assertNotNull(response.paidAt)

        val outboxCaptor = ArgumentCaptor.forClass(OutboxEvent::class.java)
        Mockito.verify(outboxEventRepository).save(outboxCaptor.capture())
        assertEquals("payment.succeeded", outboxCaptor.value.eventType)
        assertEquals("ems.payment.succeeded", outboxCaptor.value.topic)
    }

    @Test
    fun `returns receipt metadata for succeeded payment`() {
        val payment = paymentFrom(createRequest())
        Mockito.`when`(paymentRepository.findById(payment.id)).thenReturn(Optional.of(payment))
        paymentService.capturePayment(payment.id)

        val response = paymentService.getReceipt(payment.id)

        assertEquals(payment.id, response.paymentId)
        assertEquals(payment.ticketId, response.ticketId)
        assertEquals("receipts/${payment.id}.pdf", response.receiptObjectKey)
        assertNotNull(response.receiptUrl)
        assertNotNull(response.paidAt)
    }

    @Test
    fun `fails pending payment`() {
        val payment = paymentFrom(createRequest())
        Mockito.`when`(paymentRepository.findById(payment.id)).thenReturn(Optional.of(payment))

        val response = paymentService.failPayment(payment.id, "insufficient funds")

        assertEquals(PaymentStatus.FAILED, response.status)
        assertEquals("insufficient funds", response.failureReason)
        val outboxCaptor = ArgumentCaptor.forClass(OutboxEvent::class.java)
        Mockito.verify(outboxEventRepository).save(outboxCaptor.capture())
        assertEquals("payment.failed", outboxCaptor.value.eventType)
        assertEquals("ems.payment.failed", outboxCaptor.value.topic)
    }

    @Test
    fun `rejects state transition from failed payment`() {
        val payment = paymentFrom(createRequest()).apply { markFailed("declined") }
        Mockito.`when`(paymentRepository.findById(payment.id)).thenReturn(Optional.of(payment))

        assertFailsWith<PaymentStateException> {
            paymentService.capturePayment(payment.id)
        }
    }

    private fun createRequest(): CreatePaymentRequest =
        CreatePaymentRequest(
            ticketId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            eventId = UUID.randomUUID(),
            amount = BigDecimal("49.9"),
            currency = "USD",
            idempotencyKey = UUID.randomUUID().toString(),
        )

    private fun paymentFrom(request: CreatePaymentRequest): Payment =
        Payment(
            ticketId = requireNotNull(request.ticketId),
            userId = requireNotNull(request.userId),
            eventId = requireNotNull(request.eventId),
            amount = requireNotNull(request.amount).setScale(2),
            currency = request.currency,
            idempotencyKey = request.idempotencyKey,
        )

    private class RecordingReceiptStorage : ReceiptStorage {
        var lastObjectKey: String? = null
            private set
        var lastContent: ByteArray? = null
            private set

        override fun store(objectKey: String, content: ByteArray): StoredReceipt {
            lastObjectKey = objectKey
            lastContent = content
            return StoredReceipt(objectKey, "http://localhost:4566/$objectKey")
        }
    }
}
