package com.ems.apigateway.config

import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.security.oauth2.jwt.Jwt

class KeycloakRoleConverterTest {
    private val converter = KeycloakRoleConverter()

    @Test
    fun `converts realm and client roles to Spring authorities`() {
        val jwt = jwt(
            mapOf(
                "realm_access" to mapOf("roles" to listOf("organizer")),
                "resource_access" to mapOf(
                    "ems-api" to mapOf("roles" to listOf("admin", "user")),
                ),
            ),
        )

        val authorities = converter.convert(jwt).map { authority -> authority.authority }.toSet()

        assertEquals(setOf("ROLE_ORGANIZER", "ROLE_ADMIN", "ROLE_USER"), authorities)
    }

    private fun jwt(claims: Map<String, Any>): Jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("user-id")
            .claims { jwtClaims -> jwtClaims.putAll(claims) }
            .build()
}
