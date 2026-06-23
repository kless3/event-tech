package com.ems.apigateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val keycloakRoleConverter: KeycloakRoleConverter,
) {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .csrf { csrf -> csrf.disable() }
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                    .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .pathMatchers("/docs/*/v3/api-docs").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/v1/events/**").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/v1/users/**").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/v1/events/**").denyAll()
                    .pathMatchers(HttpMethod.PUT, "/api/v1/events/**").hasAnyRole(ORGANIZER, ADMIN)
                    .pathMatchers(HttpMethod.PATCH, "/api/v1/events/**").hasAnyRole(ORGANIZER, ADMIN)
                    .pathMatchers(HttpMethod.DELETE, "/api/v1/events/**").hasAnyRole(ORGANIZER, ADMIN)
                    .pathMatchers("/api/v1/organizers/**").hasAnyRole(ORGANIZER, ADMIN)
                    .pathMatchers("/api/v1/imports/**").hasAnyRole(ORGANIZER, ADMIN)
                    .pathMatchers("/api/v1/notifications/**").hasRole(ADMIN)
                    .pathMatchers("/api/v1/payments/**").hasAnyRole(USER, ORGANIZER, ADMIN)
                    .pathMatchers("/api/v1/tickets/**").hasAnyRole(USER, ORGANIZER, ADMIN)
                    .pathMatchers("/api/v1/users/**").hasAnyRole(USER, ADMIN)
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .build()

    private fun jwtAuthenticationConverter(): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        val delegate = Converter<Jwt, AbstractAuthenticationToken> { jwt ->
            JwtAuthenticationToken(jwt, keycloakRoleConverter.convert(jwt), jwt.subject ?: jwt.id ?: "jwt")
        }
        return ReactiveJwtAuthenticationConverterAdapter(delegate)
    }

    private companion object {
        const val ADMIN = "ADMIN"
        const val ORGANIZER = "ORGANIZER"
        const val USER = "USER"
    }
}
