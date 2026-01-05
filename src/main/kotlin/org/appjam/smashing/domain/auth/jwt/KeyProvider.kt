package org.appjam.smashing.domain.auth.jwt

import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.Key

@Component
class KeyProvider(
    private val jwtProperties: JwtProperties
) {
    fun getSigningKey(): Key {
        val keyBytes = Decoders.BASE64.decode(jwtProperties.secret)

        return Keys.hmacShaKeyFor(keyBytes)
    }
}
