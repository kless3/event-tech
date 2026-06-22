package com.ems.apigateway.filter

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import reactor.core.publisher.Mono

class AuthenticatedUserHeaderFilterTest {
    private val filter = AuthenticatedUserHeaderFilter()

    @Test
    fun `forwards user context from jwt`() {
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/tickets").build(),
        ).mutate()
            .principal(Mono.just(jwtAuthentication()))
            .build()

        filter.filter(exchange, captureHeadersChain { headers ->
            assertEquals(USER_ID, headers[AuthenticatedUserHeaderFilter.USER_ID_HEADER])
            assertEquals(USERNAME, headers[AuthenticatedUserHeaderFilter.USERNAME_HEADER])
            assertEquals("ORGANIZER,USER", headers[AuthenticatedUserHeaderFilter.USER_ROLES_HEADER])
        }).block()
    }

    @Test
    fun `removes spoofed user context when request is anonymous`() {
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/events")
                .header(AuthenticatedUserHeaderFilter.USER_ID_HEADER, "spoofed")
                .header(AuthenticatedUserHeaderFilter.USERNAME_HEADER, "spoofed")
                .header(AuthenticatedUserHeaderFilter.USER_ROLES_HEADER, "ADMIN")
                .build(),
        )

        filter.filter(exchange, captureHeadersChain { headers ->
            assertNull(headers[AuthenticatedUserHeaderFilter.USER_ID_HEADER])
            assertNull(headers[AuthenticatedUserHeaderFilter.USERNAME_HEADER])
            assertNull(headers[AuthenticatedUserHeaderFilter.USER_ROLES_HEADER])
        }).block()
    }

    private fun jwtAuthentication(): JwtAuthenticationToken {
        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            mapOf(
                "sub" to USER_ID,
                "preferred_username" to USERNAME,
            ),
        )
        return JwtAuthenticationToken(
            jwt,
            listOf(
                SimpleGrantedAuthority("ROLE_USER"),
                SimpleGrantedAuthority("ROLE_ORGANIZER"),
            ),
        )
    }

    private fun captureHeadersChain(assertHeaders: (Map<String, String?>) -> Unit): GatewayFilterChain =
        GatewayFilterChain { exchange ->
            assertHeaders(
                mapOf(
                    AuthenticatedUserHeaderFilter.USER_ID_HEADER to
                        exchange.request.headers.getFirst(AuthenticatedUserHeaderFilter.USER_ID_HEADER),
                    AuthenticatedUserHeaderFilter.USERNAME_HEADER to
                        exchange.request.headers.getFirst(AuthenticatedUserHeaderFilter.USERNAME_HEADER),
                    AuthenticatedUserHeaderFilter.USER_ROLES_HEADER to
                        exchange.request.headers.getFirst(AuthenticatedUserHeaderFilter.USER_ROLES_HEADER),
                ),
            )
            exchange.response.setComplete()
        }

    companion object {
        private const val USER_ID = "26859a47-64da-48e2-90ee-673ac99f1a7b"
        private const val USERNAME = "organizer@ems.local"
    }
}
