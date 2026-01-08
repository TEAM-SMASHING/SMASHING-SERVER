package org.appjam.smashing.domain.auth.controller

import jakarta.validation.Valid
import org.appjam.smashing.domain.auth.dto.request.SignInRequest
import org.appjam.smashing.domain.auth.dto.response.SignInResponse
import org.appjam.smashing.domain.auth.service.AuthService
import org.appjam.smashing.global.common.dto.ApiResponse
import org.appjam.smashing.global.common.enums.SuccessCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/login/kakao")
    fun signIn(
        @Valid @RequestBody signInRequest: SignInRequest
    ): ResponseEntity<ApiResponse<SignInResponse>> {
        val response: SignInResponse = authService.signIn(signInRequest.toCommand())

        return if (response.isCompletedSignUp()) {
            ApiResponse.success(
                data = response
            )
        } else {
            ApiResponse.success(
                status = SuccessCode.ACCEPTED.httpStatus,
                data = response
            )
        }
    }
}
