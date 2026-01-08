package org.appjam.smashing.domain.auth.social.kakao

import org.appjam.smashing.domain.auth.test.dto.KakaoUserResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader

/**
 * [KakaoApiClient]
 *
 * 카카오 API 서버와 통신하여 사용자 정보를 가져오기 위한 Feign Client
 */
@FeignClient(name = "kakaoApiClient", url = "https://kapi.kakao.com")
interface KakaoApiClient {
    @GetMapping("/v2/user/me")
    fun getUserInfo(
        @RequestHeader(HttpHeaders.AUTHORIZATION) bearerToken: String
    ): KakaoUserResponse
}
