package com.ems.userservice.config

import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CryptoConfigTest {
    private val cryptoConfig = CryptoConfig()

    @Test
    fun `creates aes key from 256 bit base64 kek`() {
        val kekBase64 = Base64.getEncoder().encodeToString(ByteArray(32) { 7 })

        val key = cryptoConfig.keyEncryptionKey(SecurityProperties(kekBase64 = kekBase64))

        assertEquals("AES", key.algorithm)
        assertEquals(32, key.encoded.size)
    }

    @Test
    fun `rejects non 256 bit kek`() {
        val invalidKekBase64 = Base64.getEncoder().encodeToString(ByteArray(16))

        assertFailsWith<IllegalArgumentException> {
            cryptoConfig.keyEncryptionKey(SecurityProperties(kekBase64 = invalidKekBase64))
        }
    }
}
