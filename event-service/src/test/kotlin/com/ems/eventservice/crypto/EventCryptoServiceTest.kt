package com.ems.eventservice.crypto

import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class EventCryptoServiceTest {
    private val cryptoService = EventCryptoService()
    private val dekBase64 = Base64.getEncoder().encodeToString(ByteArray(32) { 5 })

    @Test
    fun `encrypts and decrypts organizer note`() {
        val encrypted = cryptoService.encrypt("private organizer note", dekBase64)

        val decrypted = cryptoService.decrypt(encrypted.ciphertextBase64, encrypted.ivBase64, dekBase64)

        assertEquals("private organizer note", decrypted)
        assertNotEquals("private organizer note", encrypted.ciphertextBase64)
        assertEquals(12, Base64.getDecoder().decode(encrypted.ivBase64).size)
    }

    @Test
    fun `rejects non 256 bit dek`() {
        val invalidDekBase64 = Base64.getEncoder().encodeToString(ByteArray(16))

        assertFailsWith<IllegalArgumentException> {
            cryptoService.encrypt("note", invalidDekBase64)
        }
    }
}
