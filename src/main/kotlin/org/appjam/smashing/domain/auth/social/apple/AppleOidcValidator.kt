package org.appjam.smashing.domain.auth.social.apple

import org.appjam.smashing.domain.auth.social.OidcTokenValidator
import org.springframework.stereotype.Component

@Component
class AppleOidcValidator(
    private val oidcTokenValidator: OidcTokenValidator,
) {
    fun extractAppleId(idToken: String): String =
        oidcTokenValidator.extractSocialId(
            idToken = idToken,
            jwksUri = JWKS_URI,
            iss = ISS,
        )

    companion object {
        private const val JWKS_URI = "https://appleid.apple.com/auth/keys"
        private const val ISS = "https://appleid.apple.com"
    }
}
