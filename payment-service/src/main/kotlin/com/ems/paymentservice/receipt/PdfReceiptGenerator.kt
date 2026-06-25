package com.ems.paymentservice.receipt

import java.io.ByteArrayOutputStream
import org.springframework.stereotype.Component

@Component
class PdfReceiptGenerator {
    fun generate(receipt: PaymentReceipt): ByteArray {
        val lines = listOf(
            "Booking Platform",
            "Payment receipt",
            "Payment ID: ${receipt.paymentId}",
            "Ticket ID: ${receipt.ticketId}",
            "User ID: ${receipt.userId}",
            "Event ID: ${receipt.eventId}",
            "Amount: ${receipt.amount} ${receipt.currency}",
            "Paid at: ${receipt.paidAt}",
        )
        val textStream = buildString {
            append("BT\n/F1 14 Tf\n72 760 Td\n")
            lines.forEachIndexed { index, line ->
                if (index > 0) {
                    append("0 -26 Td\n")
                }
                append("(").append(line.escapePdfText()).append(") Tj\n")
            }
            append("ET")
        }
        val objects = listOf(
            "<< /Type /Catalog /Pages 2 0 R >>",
            "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
            "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
            "<< /Length ${textStream.toByteArray().size} >>\nstream\n$textStream\nendstream",
        )

        val output = ByteArrayOutputStream()
        output.write("%PDF-1.4\n".toByteArray())
        val offsets = mutableListOf(0)
        objects.forEachIndexed { index, content ->
            offsets += output.size()
            output.write("${index + 1} 0 obj\n$content\nendobj\n".toByteArray())
        }
        val xrefOffset = output.size()
        output.write("xref\n0 ${objects.size + 1}\n".toByteArray())
        output.write("0000000000 65535 f \n".toByteArray())
        offsets.drop(1).forEach { offset ->
            output.write(String.format("%010d 00000 n \n", offset).toByteArray())
        }
        output.write(
            """
            trailer
            << /Size ${objects.size + 1} /Root 1 0 R >>
            startxref
            $xrefOffset
            %%EOF
            """.trimIndent().toByteArray(),
        )
        return output.toByteArray()
    }

    private fun String.escapePdfText(): String =
        replace("\\", "\\\\")
            .replace("(", "\\(")
            .replace(")", "\\)")
}
