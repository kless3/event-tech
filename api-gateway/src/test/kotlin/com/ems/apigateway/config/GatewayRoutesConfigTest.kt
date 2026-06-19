package com.ems.apigateway.config

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.gateway.route.RouteLocator

@SpringBootTest(
    properties = [
        "app.routes.user-service-base-url=http://users",
        "app.routes.ticket-service-base-url=http://tickets",
        "app.routes.event-service-base-url=http://events",
        "app.routes.payment-service-base-url=http://payments",
    ],
)
class GatewayRoutesConfigTest {
    @Autowired
    private lateinit var routeLocator: RouteLocator

    @Test
    fun `registers public service routes`() {
        val routes = routeLocator.routes.collectList().block() ?: emptyList()
        val routeIds = routes.map { route -> route.id }

        assertEquals(4, routes.size)
        assertContains(routeIds, "user-service")
        assertContains(routeIds, "ticket-service")
        assertContains(routeIds, "event-service")
        assertContains(routeIds, "payment-service")
    }
}
