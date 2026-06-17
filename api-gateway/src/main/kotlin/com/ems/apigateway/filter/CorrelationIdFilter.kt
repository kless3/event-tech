package com.ems.apigateway.filter

import java.util.UUID
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class CorrelationIdFilter : GlobalFilter, Ordered {
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val correlationId = exchange.request.headers.getFirst(CORRELATION_ID_HEADER) ?: UUID.randomUUID().toString()
        val request = exchange.request.mutate()
            .headers { headers -> headers.set(CORRELATION_ID_HEADER, correlationId) }
            .build()

        exchange.response.beforeCommit {
            exchange.response.headers.set(CORRELATION_ID_HEADER, correlationId)
            Mono.empty()
        }

        return chain.filter(exchange.mutate().request(request).build())
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

    companion object {
        const val CORRELATION_ID_HEADER = "X-Correlation-Id"
    }
}
