package com.ems.eventservice.crypto

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.springframework.stereotype.Service

@Service
class EventCryptoService {
    private val secureRandom = SecureRandom()
    private val base64Encoder = Base64.getEncoder()
    private val base64Decoder = Base64.getDecoder()

    fun encrypt(plainText: String, dekBase64: String): EncryptedValue {
        val iv = ByteArray(GCM_IV_SIZE_BYTES).also(secureRandom::nextBytes)
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keyFrom(dekBase64), GCMParameterSpec(GCM_TAG_SIZE_BITS, iv))
        return EncryptedValue(
            ciphertextBase64 = base64Encoder.encodeToString(cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))),
            ivBase64 = base64Encoder.encodeToString(iv),
        )
    }

    fun decrypt(ciphertextBase64: String, ivBase64: String, dekBase64: String): String {
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            keyFrom(dekBase64),
            GCMParameterSpec(GCM_TAG_SIZE_BITS, base64Decoder.decode(ivBase64)),
        )
        return cipher.doFinal(base64Decoder.decode(ciphertextBase64)).toString(Charsets.UTF_8)
    }

    private fun keyFrom(dekBase64: String): SecretKeySpec {
        val decoded = base64Decoder.decode(dekBase64)
        require(decoded.size == AES_256_KEY_SIZE_BYTES) {
            "User DEK must contain a 256-bit AES key encoded as Base64"
        }
        return SecretKeySpec(decoded, AES_ALGORITHM)
    }

    private companion object {
        const val AES_ALGORITHM = "AES"
        const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        const val AES_256_KEY_SIZE_BYTES = 32
        const val GCM_IV_SIZE_BYTES = 12
        const val GCM_TAG_SIZE_BITS = 128
    }
}

data class EncryptedValue(
    val ciphertextBase64: String,
    val ivBase64: String,
)
