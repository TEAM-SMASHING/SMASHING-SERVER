package org.appjam.smashing.domain.auth.social.kakao

import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.stereotype.Component

@Component
class KakaoAuthTokenValidator(
    private val kakaoApiClient: KakaoApiClient
) {
    fun extractKakaoId(authAccessToken: String): String =
        try {
            val token = if (authAccessToken.startsWith(PREFIX)) authAccessToken else "$PREFIX$authAccessToken"

            val response = kakaoApiClient.getUserInfo(token)

            response.id.toString()

        } catch (e: Exception) {
            throw CustomException(ErrorCode.INVALID_KAKAO_ACCESS_TOKEN)
        }

    companion object {
        private const val PREFIX = "Bearer "
    }
}
