package org.appjam.smashing.domain.auth.controller

import org.appjam.smashing.domain.auth.command.reqeust.SignInRequestCommand.Companion.toCommand
import org.appjam.smashing.domain.auth.command.response.SignInResponseCommand.Companion.toDto
import org.appjam.smashing.domain.auth.dto.request.SignInRequest
import org.appjam.smashing.domain.auth.dto.request.SignUpRequest
import org.appjam.smashing.domain.auth.dto.response.SignInResponse
import org.appjam.smashing.domain.auth.service.AuthService
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/login/kakao")
    fun signIn(
        @RequestBody signInRequest: SignInRequest
    ): ResponseEntity<ApiResponse<SignInResponse>> {
        val response: SignInResponse = authService.signIn(signInRequest.toCommand()).toDto()

        return ApiResponse.success(
            data = response
        )
    }

    @PostMapping("/signup")
    fun signUp(
        @RequestHeader("Auth-Id") authId: String,
        @RequestBody signUpRequest: SignUpRequest,
    ) {

    }
}
