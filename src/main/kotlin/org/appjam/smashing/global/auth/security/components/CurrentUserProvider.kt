package org.appjam.smashing.global.auth.security.components

import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class CurrentUserProvider {

    fun currentUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AuthenticationCredentialsNotFoundException("Unauthenticated")

        if (!authentication.isAuthenticated) {
            throw InsufficientAuthenticationException("Unauthenticated")
        }

        return when (val principal = authentication.principal) {
            is CustomUserDetails -> principal.username
            else -> throw InsufficientAuthenticationException(
                "Unsupported principal: ${principal?.javaClass?.name}"
            )
        }
    }
}
