package org.appjam.smashing.domain.auth.kakao.controller

import org.appjam.smashing.domain.auth.kakao.service.KakaoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * [KakaoController]
 *
 * 카카오 로그인 성공 시 인가 코드를 전달받는 Callback 엔드포인트
 *
 * * 테스트용 URL
 * * https://kauth.kakao.com/oauth/authorize?client_id={REST_API_KEY}&redirect_uri=http://localhost:8080/api/oauth/kakao/callback&response_type=code
 */
@RestController
@RequestMapping("/api/oauth")
class KakaoController(
    private val kakaoService: KakaoService
) {
    @GetMapping("/kakao/callback")
    fun kakaoCallback(@RequestParam code: String): ResponseEntity<String> = ResponseEntity.ok(kakaoService.login(code))
}
