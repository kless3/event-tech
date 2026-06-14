package com.ems.userservice.crypto

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.springframework.stereotype.Service

@Service
class CryptoService(
    private val keyEncryptionKey: SecretKey,
) {
    private val secureRandom = SecureRandom()
    private val base64Encoder = Base64.getEncoder()
    private val base64Decoder = Base64.getDecoder()

    fun generateDek(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM)
        keyGenerator.init(AES_256_KEY_SIZE_BITS, secureRandom)
        return keyGenerator.generateKey()
    }

    fun encryptDek(dek: SecretKey): EncryptedDek {
        val iv = ByteArray(GCM_IV_SIZE_BYTES).also(secureRandom::nextBytes)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keyEncryptionKey, GCMParameterSpec(GCM_TAG_SIZE_BITS, iv))

        val ciphertext = cipher.doFinal(dek.encoded)
        return EncryptedDek(
            ciphertextBase64 = base64Encoder.encodeToString(ciphertext),
            ivBase64 = base64Encoder.encodeToString(iv),
        )
    }

    fun decryptDekToBase64(encryptedDekBase64: String, ivBase64: String): String {
        val iv = base64Decoder.decode(ivBase64)
        val encryptedDek = base64Decoder.decode(encryptedDekBase64)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keyEncryptionKey, GCMParameterSpec(GCM_TAG_SIZE_BITS, iv))

        val dekBytes = cipher.doFinal(encryptedDek)
        return base64Encoder.encodeToString(SecretKeySpec(dekBytes, AES_ALGORITHM).encoded)
    }

    companion object {
        private const val AES_ALGORITHM = "AES"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val AES_256_KEY_SIZE_BITS = 256
        private const val GCM_IV_SIZE_BYTES = 12
        private const val GCM_TAG_SIZE_BITS = 128
    }
}

data class EncryptedDek(
    val ciphertextBase64: String,
    val ivBase64: String,
)
