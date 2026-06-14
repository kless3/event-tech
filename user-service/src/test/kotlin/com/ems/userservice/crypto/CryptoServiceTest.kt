package com.ems.userservice.crypto

import java.util.Base64
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CryptoServiceTest {
    private val kek = SecretKeySpec(ByteArray(32) { it.toByte() }, "AES")
    private val cryptoService = CryptoService(kek)

    @Test
    fun `encrypts and decrypts generated user dek`() {
        val dek = cryptoService.generateDek()
        val encryptedDek = cryptoService.encryptDek(dek)

        val decryptedDekBase64 = cryptoService.decryptDekToBase64(
            encryptedDekBase64 = encryptedDek.ciphertextBase64,
            ivBase64 = encryptedDek.ivBase64,
        )

        assertEquals(Base64.getEncoder().encodeToString(dek.encoded), decryptedDekBase64)
        assertNotEquals(decryptedDekBase64, encryptedDek.ciphertextBase64)
    }

    @Test
    fun `generates 256 bit dek and 96 bit gcm iv`() {
        val dek = cryptoService.generateDek()
        val encryptedDek = cryptoService.encryptDek(dek)

        assertEquals(32, dek.encoded.size)
        assertEquals(12, Base64.getDecoder().decode(encryptedDek.ivBase64).size)
        assertTrue(encryptedDek.ciphertextBase64.isNotBlank())
    }
}
