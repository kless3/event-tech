package com.ems.apigateway.config

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class KeycloakRoleConverter {
    fun convert(jwt: Jwt): Collection<GrantedAuthority> =
        buildSet {
            addAll(realmRoles(jwt))
            addAll(clientRoles(jwt))
        }.map { role -> SimpleGrantedAuthority("ROLE_${role.uppercase()}") }

    private fun realmRoles(jwt: Jwt): Collection<String> {
        val realmAccess = jwt.claims["realm_access"] as? Map<*, *> ?: return emptySet()
        return rolesFrom(realmAccess)
    }

    private fun clientRoles(jwt: Jwt): Collection<String> {
        val resourceAccess = jwt.claims["resource_access"] as? Map<*, *> ?: return emptySet()
        return resourceAccess.values
            .filterIsInstance<Map<*, *>>()
            .flatMap { access -> rolesFrom(access) }
            .toSet()
    }

    private fun rolesFrom(access: Map<*, *>): Collection<String> =
        (access["roles"] as? Collection<*>)
            ?.filterIsInstance<String>()
            ?.toSet()
            .orEmpty()
}
