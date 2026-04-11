package org.appjam.smashing.domain.auth.social.kakao

import org.appjam.smashing.domain.auth.social.OidcTokenValidator
import org.springframework.stereotype.Component

@Component
class KakaoOidcValidator(
    private val oidcTokenValidator: OidcTokenValidator,
) {
    fun extractKakaoId(idToken: String): String =
        oidcTokenValidator.extractSocialId(
            idToken = idToken,
            jwksUri = JWKS_URI,
            iss = ISS,
        )

    companion object {
        private const val JWKS_URI = "https://kauth.kakao.com/.well-known/jwks.json"
        private const val ISS = "https://kauth.kakao.com"
    }
}
