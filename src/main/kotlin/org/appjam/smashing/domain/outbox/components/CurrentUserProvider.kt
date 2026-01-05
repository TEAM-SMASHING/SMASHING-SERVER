package org.appjam.smashing.domain.outbox.components

import org.appjam.smashing.domain.auth.jwt.CustomUserDetails
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class CurrentUserProvider {
    // TODO: jwt 완료 이후 패키지 분리

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
