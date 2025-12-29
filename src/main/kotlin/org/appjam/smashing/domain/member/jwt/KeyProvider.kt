package org.appjam.smashing.domain.member.jwt

import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class KeyProvider(
    private val jwtProperties: JwtProperties
) {
    fun getSigningKey(): Key =
        Keys.hmacShaKeyFor(encodeSecretKeyToBase64().toByteArray())

    private fun encodeSecretKeyToBase64(): String =
        Base64.getEncoder().encodeToString(jwtProperties.secret.toByteArray())
}
