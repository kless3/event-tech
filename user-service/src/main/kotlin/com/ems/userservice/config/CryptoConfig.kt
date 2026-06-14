package com.ems.userservice.config

import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SecurityProperties::class)
class CryptoConfig {
    @Bean
    fun keyEncryptionKey(properties: SecurityProperties): SecretKey {
        val decodedKey = Base64.getDecoder().decode(properties.kekBase64)
        require(decodedKey.size == AES_256_KEY_SIZE_BYTES) {
            "app.security.kek-base64 must contain a 256-bit AES key encoded as Base64"
        }
        return SecretKeySpec(decodedKey, "AES")
    }

    private companion object {
        const val AES_256_KEY_SIZE_BYTES = 32
    }
}
