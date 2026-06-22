package com.ems.apigateway.filter

import java.security.Principal
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthenticatedUserHeaderFilter : GlobalFilter, Ordered {
    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> =
        exchange.getPrincipal<Principal>()
            .cast(Authentication::class.java)
            .filter(Authentication::isAuthenticated)
            .map { authentication -> withAuthenticatedHeaders(exchange, authentication) }
            .defaultIfEmpty(withoutAuthenticatedHeaders(exchange))
            .flatMap(chain::filter)

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 20

    private fun withAuthenticatedHeaders(
        exchange: ServerWebExchange,
        authentication: Authentication,
    ): ServerWebExchange {
        val jwt = (authentication as? JwtAuthenticationToken)?.token
        val userId = jwt?.subject ?: authentication.name
        val username = jwt?.getClaimAsString("preferred_username")
            ?: jwt?.getClaimAsString("email")
            ?: authentication.name
        val roles = authentication.authorities
            .mapNotNull { authority -> authority.authority?.removePrefix("ROLE_") }
            .sorted()
            .joinToString(",")

        val request = exchange.request.mutate()
            .headers { headers ->
                clearAuthenticatedHeaders(headers)
                headers.set(USER_ID_HEADER, userId)
                headers.set(USERNAME_HEADER, username)
                headers.set(USER_ROLES_HEADER, roles)
            }
            .build()
        return exchange.mutate().request(request).build()
    }

    private fun withoutAuthenticatedHeaders(exchange: ServerWebExchange): ServerWebExchange {
        val request = exchange.request.mutate()
            .headers(::clearAuthenticatedHeaders)
            .build()
        return exchange.mutate().request(request).build()
    }

    private fun clearAuthenticatedHeaders(headers: org.springframework.http.HttpHeaders) {
        headers.remove(USER_ID_HEADER)
        headers.remove(USERNAME_HEADER)
        headers.remove(USER_ROLES_HEADER)
    }

    companion object {
        const val USER_ID_HEADER = "X-Authenticated-User-Id"
        const val USERNAME_HEADER = "X-Authenticated-Username"
        const val USER_ROLES_HEADER = "X-Authenticated-User-Roles"
    }
}
