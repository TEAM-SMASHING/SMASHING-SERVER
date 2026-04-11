package org.appjam.smashing.domain.auth.social

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.*

@Component
class OidcTokenValidator {
    fun extractSocialId(
        idToken: String,
        jwksUri: String,
        iss: String,
    ): String = try {
        val publicKey = getPublicKey(idToken, jwksUri)

        val claims = Jwts.parserBuilder()
            .setSigningKey(publicKey)
            .build()
            .parseClaimsJws(idToken)
            .body

        require(claims.issuer == iss) { "invalid iss" }

        claims.subject
    } catch (e: Exception) {
        throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN) // todo: change
    }

    private fun getPublicKey(
        idToken: String,
        jwksUri: String,
    ): PublicKey {
        val header = String(Base64.getUrlDecoder().decode(idToken.split(".")[0]))
        val kid = ObjectMapper().readTree(header).get("kid").asText()

        val jwks = RestTemplate().getForObject(jwksUri, JsonNode::class.java)
            ?: throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN) // todo: change

        val key = jwks["keys"].find { it["kid"].asText() == kid }
            ?: throw CustomException(ErrorCode.INVALID_ACCESS_TOKEN) // todo: change

        val n = BigInteger(1, Base64.getUrlDecoder().decode(key["n"].asText()))
        val e = BigInteger(1, Base64.getUrlDecoder().decode(key["e"].asText()))

        return KeyFactory.getInstance("RSA")
            .generatePublic(RSAPublicKeySpec(n, e))
    }
}
