package org.appjam.smashing.domain.auth.social

import org.appjam.smashing.domain.auth.dto.command.SignInRequestCommand
import org.appjam.smashing.domain.auth.enums.ProviderType
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
    fun getSocialId(command: SignInRequestCommand): Pair<ProviderType, String> {
        val idToken = command.idToken

        return when (command.provider) {
            KAKAO -> Pair(KAKAO, kakaoOidcValidator.extractKakaoId(idToken))
            APPLE -> Pair(APPLE, appleOidcValidator.extractAppleId(idToken))
        }
    }
}
