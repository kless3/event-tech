package com.ems.apigateway.filter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import reactor.core.publisher.Mono

class CorrelationIdFilterTest {
    private val filter = CorrelationIdFilter()

    @Test
    fun `keeps incoming correlation id`() {
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/v1/events")
                .header(CorrelationIdFilter.CORRELATION_ID_HEADER, CORRELATION_ID)
                .build(),
        )

        filter.filter(exchange, captureRequestHeaderChain { correlationId ->
            assertEquals(CORRELATION_ID, correlationId)
        }).block()

        assertEquals(CORRELATION_ID, exchange.response.headers.getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER))
    }

    @Test
    fun `creates correlation id when request does not provide one`() {
        val exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/events").build())
        var generatedCorrelationId: String? = null

        filter.filter(exchange, captureRequestHeaderChain { correlationId ->
            generatedCorrelationId = correlationId
        }).block()

        assertNotNull(generatedCorrelationId)
        assertEquals(generatedCorrelationId, exchange.response.headers.getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER))
    }

    private fun captureRequestHeaderChain(assertHeader: (String?) -> Unit): GatewayFilterChain =
        GatewayFilterChain { exchange ->
            assertHeader(exchange.request.headers.getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER))
            exchange.response.setComplete()
        }

    companion object {
        private const val CORRELATION_ID = "trace-123"
    }
}
