package org.appjam.smashing.domain.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.appjam.smashing.domain.auth.dto.request.SignInRequest
import org.appjam.smashing.domain.auth.dto.request.SignUpRequest
import org.appjam.smashing.domain.auth.dto.request.TokenReissueRequest
import org.appjam.smashing.domain.auth.dto.response.SignInResponse
import org.appjam.smashing.domain.auth.dto.response.SignUpResponse
import org.appjam.smashing.domain.auth.dto.response.TokenReissueResponse
import org.appjam.smashing.domain.auth.service.AuthService
import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.appjam.smashing.global.common.dto.ApiResponse
import org.appjam.smashing.global.common.enums.SuccessCode
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @Operation(
        summary = "카카오 로그인 API",
        description = "카카오 로그인을 진행합니다."
    )
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

    @Operation(
        summary = "회원가입 API",
        description = "회원가입을 진행합니다."
    )
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody signUpRequest: SignUpRequest,
    ): ResponseEntity<ApiResponse<SignUpResponse>> {
        val response = authService.signUp(signUpRequest.toCommand())

        return ApiResponse.success(
            data = response
        )
    }

    @Operation(
        summary = "로그아웃 API",
        description = "로그아웃을 진행합니다."
    )
    @PostMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") accessToken: String,
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<ApiResponse<Unit>> {
        authService.logout(
            accessToken = accessToken,
            userId = principal.username,
        )

        return ApiResponse.success()
    }

    @Operation(
        summary = "토큰 재발급 API",
        description = "엑세스 토큰 만료시, 엑세스 토큰과 리프레시 토큰을 재발급 합니다."
    )
    @PostMapping("/reissue")
    fun tokenReissue(
        @Valid @RequestBody tokenReissueRequest: TokenReissueRequest,
    ): ResponseEntity<ApiResponse<TokenReissueResponse>> {
        val response = authService.tokenReissue(
            reqeustCommand = tokenReissueRequest.toCommand(),
        )

        return ApiResponse.success(
            data = response
        )
    }

    @Operation(
        summary = "탈퇴 API",
        description = "회원을 탈퇴합니다."
    )
    @PostMapping("/withdraw")
    fun withdraw(
        @RequestHeader("Authorization") accessToken: String,
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<ApiResponse<Unit>> {
        authService.withdraw(
            accessToken = accessToken,
            userId = principal.username
        )

        return ApiResponse.success()
    }
}
