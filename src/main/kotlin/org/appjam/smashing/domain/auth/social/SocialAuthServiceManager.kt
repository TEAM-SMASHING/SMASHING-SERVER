package org.appjam.smashing.domain.auth.social

import org.appjam.smashing.domain.auth.dto.command.SignInRequestCommand
import org.appjam.smashing.domain.auth.dto.response.SocialType
import org.springframework.stereotype.Component

@Component
class SocialAuthServiceManager(
    private val oidcTokenValidator: OidcTokenValidator,
) {
    fun getSocialId(command: SignInRequestCommand): SocialType {
        val socialId = oidcTokenValidator.extractSocialId(
            idToken = command.idToken,
            providerType = command.provider
        )

        return SocialType(
            provider = command.provider,
            socialId = socialId,
        )
    }
}
