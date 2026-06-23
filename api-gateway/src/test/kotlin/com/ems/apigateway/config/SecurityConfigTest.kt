package com.ems.apigateway.config

import kotlin.test.Test
import kotlin.test.assertNotEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.junit.jupiter.api.BeforeEach
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "app.routes.user-service-base-url=http://users",
        "app.routes.ticket-service-base-url=http://tickets",
        "app.routes.event-service-base-url=http://events",
        "app.routes.payment-service-base-url=http://payments",
        "app.routes.notification-service-base-url=http://notifications",
        "app.routes.importer-service-base-url=http://imports",
    ],
)
class SecurityConfigTest {
    @Autowired
    private lateinit var applicationContext: ApplicationContext

    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webTestClient = WebTestClient
            .bindToApplicationContext(applicationContext)
            .apply(springSecurity())
            .configureClient()
            .build()
    }

    @Test
    fun `rejects organizer route without jwt`() {
        webTestClient.post()
            .uri("/api/v1/imports/ticketmaster")
            .exchange()
            .expectStatus()
            .isUnauthorized
    }

    @Test
    fun `forbids organizer route for regular user`() {
        webTestClient.mutateWith(jwtWithRoles("user"))
            .post()
            .uri("/api/v1/imports/ticketmaster")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `allows organizer route for organizer`() {
        webTestClient.mutateWith(jwtWithRoles("user", "organizer"))
            .post()
            .uri("/api/v1/organizers/me/events")
            .exchange()
            .expectStatus()
            .value { status -> assertNotEquals(HttpStatus.FORBIDDEN.value(), status) }
    }

    @Test
    fun `denies direct public event creation through gateway`() {
        webTestClient.mutateWith(jwtWithRoles("organizer"))
            .post()
            .uri("/api/v1/events")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `forbids event cancellation for regular user`() {
        webTestClient.mutateWith(jwtWithRoles("user"))
            .delete()
            .uri("/api/v1/events/25c8d38b-b1c8-4e6b-b4b6-33f427609e39")
            .exchange()
            .expectStatus()
            .isForbidden
    }

    private fun jwtWithRoles(vararg roles: String) =
        mockJwt()
            .jwt { jwt ->
                jwt.claim("realm_access", mapOf("roles" to roles.toList()))
            }
            .authorities(roles.map { role -> SimpleGrantedAuthority("ROLE_${role.uppercase()}") })
}
