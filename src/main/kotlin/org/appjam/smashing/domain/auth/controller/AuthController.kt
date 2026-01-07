package org.appjam.smashing.domain.auth.controller

import org.appjam.smashing.domain.auth.command.SignInResponseCommand.Companion.toDto
import org.appjam.smashing.domain.auth.dto.SignInResponseDto
import org.appjam.smashing.domain.auth.service.AuthService
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/login/kakao")
    fun signIn(
        @RequestHeader("Authorization") accessToken: String,
    ): ResponseEntity<ApiResponse<SignInResponseDto>> {
        val response: SignInResponseDto = authService.signIn(accessToken).toDto()

        return ApiResponse.success(
            data = response
        )
    }

    @PostMapping("/signup")
    fun signUp(

    ) {

    }
}
