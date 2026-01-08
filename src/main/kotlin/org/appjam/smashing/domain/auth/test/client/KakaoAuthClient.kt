package org.appjam.smashing.domain.auth.test.client

import org.appjam.smashing.domain.auth.test.dto.response.KakaoTokenResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * [KakaoAuthClient]
 *
 * 카카오 인증 서버와 통신하기 위한 Feign Client
 * 인가 코드를 사용하여 카카오 엑세스 토큰 및 리프레시 토큰을 발급받습니다.
 */
@FeignClient(name = "kakaoAuthClient", url = "https://kauth.kakao.com")
interface KakaoAuthClient {
    @PostMapping("/oauth/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun getAccessToken(
        @RequestParam("grant_type") grantType: String,
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("code") code: String,
        @RequestParam("client_secret") clientSecret: String,
    ): KakaoTokenResponse
}
