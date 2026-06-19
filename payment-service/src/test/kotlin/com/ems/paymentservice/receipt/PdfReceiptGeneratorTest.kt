package com.ems.paymentservice.receipt

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class PdfReceiptGeneratorTest {
    private val generator = PdfReceiptGenerator()

    @Test
    fun `generates valid looking pdf receipt`() {
        val paymentId = UUID.randomUUID()

        val pdf = generator.generate(
            PaymentReceipt(
                paymentId = paymentId,
                ticketId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                eventId = UUID.randomUUID(),
                amount = BigDecimal("49.90"),
                currency = "USD",
                paidAt = LocalDateTime.parse("2026-06-19T10:00:00"),
            ),
        )
        val content = pdf.toString(Charsets.UTF_8)

        assertTrue(content.startsWith("%PDF-1.4"))
        assertContains(content, "Payment receipt")
        assertContains(content, paymentId.toString())
        assertContains(content, "%%EOF")
    }
}
