package org.appjam.smashing.domain.user.controller

import org.appjam.smashing.domain.user.dto.request.OpenChatValidateRequest
import org.appjam.smashing.domain.user.dto.response.NicknameCheckResponse
import org.appjam.smashing.domain.user.dto.response.OpenChatValidateResponse
import org.appjam.smashing.domain.user.dto.response.UserProfileTierResponse
import org.appjam.smashing.domain.user.service.UserService
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/nickname-availability")
    fun checkNicknameAvailability(
        @RequestParam("nickname") nickname: String,
    ): ResponseEntity<ApiResponse<NicknameCheckResponse>> {
        val response = userService.checkNicknameAvailability(nickname)

        return ApiResponse.success(
            data = response
        )
    }

    @PostMapping("/openchat/validate")
    fun validateOpenChatUrl(
        @RequestBody openChatValidateRequest: OpenChatValidateRequest,
    ): ResponseEntity<ApiResponse<OpenChatValidateResponse>> {
        val response = userService.validateOpenChatUrl(openChatValidateRequest.toCommand())

        return ApiResponse.success(
            data = response
        )
    }

    @GetMapping("/me/profiles/tier")
    fun getUserProfileTier(
        @AuthenticationPrincipal userId: String,
    ): ResponseEntity<ApiResponse<UserProfileTierResponse>> {
        val response = userService.getUserProfileTier(userId)

        return ApiResponse.success(
            data = response
        )
    }
}
