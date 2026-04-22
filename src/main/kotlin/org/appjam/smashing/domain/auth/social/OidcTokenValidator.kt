package org.appjam.smashing.domain.auth.social

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import org.appjam.smashing.domain.auth.enums.ProviderType
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.*

@Component
class OidcTokenValidator(
    private val oidcProperties: OidcProperties,
    private val jwksClient: OidcJwksClient,
) {
    fun extractSocialId(
        idToken: String,
        providerType: ProviderType,
    ): String {
        // 회웑 정보 가져오기
        val (iss, jwksUri) = when (providerType) {
            ProviderType.KAKAO -> KAKAO_ISS to KAKAO_JWKS_URI
            ProviderType.APPLE -> APPLE_ISS to APPLE_JWKS_URI
        }
        val clientId = oidcProperties.getClientId(providerType)

        return try {
            val publicKey = getPublicKey(
                idToken = idToken,
                jwksUri = jwksUri,
            )

            val claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(idToken)
                .body

            // 회원 검증
            if (claims.issuer != iss) throw CustomException(ErrorCode.INVALID_ISS)
            if (claims.audience != clientId) throw CustomException(ErrorCode.INVALID_AUD)

            claims.subject
        } catch (e: Exception) {
            throw CustomException(ErrorCode.INVALID_ID_TOKEN)
        }
    }

    private fun getPublicKey(
        idToken: String,
        jwksUri: String,
    ): PublicKey {
        val header = String(Base64.getUrlDecoder().decode(idToken.split(".")[0]))
        val kid = ObjectMapper().readTree(header).get(KID).asText()

        val keys = jwksClient.getKeys(jwksUri)

        val key = keys[KEYS].find { it[KID].asText() == kid }
            ?: throw CustomException(ErrorCode.INVALID_ID_TOKEN)

        val n = BigInteger(1, Base64.getUrlDecoder().decode(key[N].asText()))
        val e = BigInteger(1, Base64.getUrlDecoder().decode(key[E].asText()))

        return KeyFactory.getInstance(RSA)
            .generatePublic(RSAPublicKeySpec(n, e))
    }

    companion object {
        private const val KID = "kid"
        private const val KEYS = "keys"
        private const val N = "n"
        private const val E = "e"
        private const val RSA = "RSA"
        private const val KAKAO_JWKS_URI = "https://kauth.kakao.com/.well-known/jwks.json"
        private const val KAKAO_ISS = "https://kauth.kakao.com"
        private const val APPLE_JWKS_URI = "https://appleid.apple.com/auth/keys"
        private const val APPLE_ISS = "https://appleid.apple.com"
    }
}
