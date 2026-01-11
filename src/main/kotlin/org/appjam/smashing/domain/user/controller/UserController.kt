package org.appjam.smashing.domain.user.controller

import jakarta.validation.Valid
import org.appjam.smashing.domain.user.dto.request.OpenChatValidateRequest
import org.appjam.smashing.domain.user.dto.request.ProfileAddRequest
import org.appjam.smashing.domain.user.dto.response.NicknameCheckResponse
import org.appjam.smashing.domain.user.dto.response.OpenChatValidateResponse
import org.appjam.smashing.domain.user.dto.response.OtherUserProfilesResponse
import org.appjam.smashing.domain.user.dto.response.UserProfileTierResponse
import org.appjam.smashing.domain.user.service.UserService
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
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
        @Valid @RequestBody openChatValidateRequest: OpenChatValidateRequest,
    ): ResponseEntity<ApiResponse<OpenChatValidateResponse>> {
        val response = userService.validateOpenChatUrl(openChatValidateRequest.toCommand())

        return ApiResponse.success(
            data = response
        )
    }

    @GetMapping("/me/profiles/tier")
    fun getUserProfileTier(
        @RequestHeader("userId") userId: String, // TODO: 인증/인가 회복시 @AuthenticationPrincipal 으로 변경
    ): ResponseEntity<ApiResponse<UserProfileTierResponse>> {
        val response = userService.getUserProfileTier(userId)

        return ApiResponse.success(
            data = response
        )
    }

    @PostMapping("me/profiles")
    fun addProfile(
        @RequestHeader("userId") userId: String, // TODO: 인증/인가 회복시 @AuthenticationPrincipal 으로 변경
        @Valid @RequestBody profileAddRequest: ProfileAddRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.addProfile(
            userId = userId,
            requestCommand = profileAddRequest.toCommand()
        )

        return ApiResponse.success()
    }

    @GetMapping("/{userId}/profiles")
    fun getOtherUserProfiles(
        @RequestHeader("userId") authId: String, // TODO: 인증/인가 회복시 @AuthenticationPrincipal 으로 변경
        @PathVariable userId: String,
        @RequestParam(required = false) sportCode: String?,
    ): ResponseEntity<ApiResponse<OtherUserProfilesResponse>> {
        val response = userService.getOtherUserProfiles(
            otherUserId = userId,
            sportCode = sportCode,
        )

        return ApiResponse.success(
            data = response,
        )
    }
}
