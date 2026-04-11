package org.appjam.smashing.domain.auth.social

import org.appjam.smashing.domain.auth.dto.command.SignInRequestCommand
import org.appjam.smashing.domain.auth.dto.response.SocialType
import org.appjam.smashing.domain.auth.enums.ProviderType.APPLE
import org.appjam.smashing.domain.auth.enums.ProviderType.KAKAO
import org.appjam.smashing.domain.auth.social.apple.AppleOidcValidator
import org.appjam.smashing.domain.auth.social.kakao.KakaoOidcValidator
import org.springframework.stereotype.Component

@Component
class SocialAuthServiceManager(
    private val kakaoOidcValidator: KakaoOidcValidator,
    private val appleOidcValidator: AppleOidcValidator,
) {
    fun getSocialId(command: SignInRequestCommand): SocialType {
        val idToken = command.idToken

        return when (command.provider) {
            KAKAO -> SocialType(
                provider = KAKAO,
                socialId = kakaoOidcValidator.extractKakaoId(idToken),
            )

            APPLE -> SocialType(
                provider = APPLE,
                socialId = appleOidcValidator.extractAppleId(idToken),
            )
        }
    }
}
