package com.ems.ticketservice.crypto

import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class TicketCryptoServiceTest {
    private val cryptoService = TicketCryptoService()
    private val dekBase64 = Base64.getEncoder().encodeToString(ByteArray(32) { 3 })

    @Test
    fun `encrypts and decrypts payload`() {
        val payload = """{"holderName":"Alice","seatCode":"A-1"}"""

        val encrypted = cryptoService.encrypt(payload, dekBase64)
        val decrypted = cryptoService.decrypt(encrypted.ciphertextBase64, encrypted.ivBase64, dekBase64)

        assertEquals(payload, decrypted)
        assertNotEquals(payload, encrypted.ciphertextBase64)
        assertEquals(12, Base64.getDecoder().decode(encrypted.ivBase64).size)
    }

    @Test
    fun `rejects non 256 bit dek`() {
        val invalidDekBase64 = Base64.getEncoder().encodeToString(ByteArray(16))

        assertFailsWith<IllegalArgumentException> {
            cryptoService.encrypt("payload", invalidDekBase64)
        }
    }
}
