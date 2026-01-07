package org.appjam.smashing.domain.auth.social

import org.springframework.stereotype.Component

@Component
class SocialAuthServiceManager(
    private val kakaoAuthTokenValidator: KakaoAuthTokenValidator,
) {
    fun getKakaoId(authAccessToken: String): String = kakaoAuthTokenValidator.extractKakaoId(authAccessToken)
}
