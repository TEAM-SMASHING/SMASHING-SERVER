package org.appjam.smashing.domain.auth.kakao.service

import org.appjam.smashing.domain.auth.kakao.client.KakaoAuthClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class KakaoService(
    private val kakaoAuthClient: KakaoAuthClient,
    @Value("\${kakao.client-id}")
    private val clientId: String,
    @Value("\${kakao.redirect-uri}")
    private val redirectUri: String,
    @Value("\${kakao.client-secret}")
    private val clientSecret: String,
) {
    fun login(code: String): String {
        val tokenResponse = kakaoAuthClient.getAccessToken(
            grantType = "authorization_code",
            clientId = clientId,
            redirectUri = redirectUri,
            code = code,
            clientSecret = clientSecret
        )

        val kakaoAccessToken = tokenResponse.accessToken

        return kakaoAccessToken
    }
}
