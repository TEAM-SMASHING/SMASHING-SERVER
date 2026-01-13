package org.appjam.smashing.domain.user.controller

import jakarta.validation.Valid
import org.appjam.smashing.domain.user.dto.request.ActiveProfileUpdateRequest
import org.appjam.smashing.domain.user.dto.request.AddressUpdateRequest
import org.appjam.smashing.domain.user.dto.request.OpenChatValidateRequest
import org.appjam.smashing.domain.user.dto.request.ProfileAddRequest
import org.appjam.smashing.domain.user.dto.response.*
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

    @PostMapping("/me/profiles")
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

    @GetMapping("/me/profiles")
    fun getUserProfiles(
        @RequestHeader("userId") userId: String, // TODO: 인증/인가 회복시 @AuthenticationPrincipal 으로 변경
    ): ResponseEntity<ApiResponse<UserProfilesResponse>> {
        val response = userService.getUserProfiles(
            userId = userId,
        )

        return ApiResponse.success(
            data = response,
        )
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

    @PutMapping("/me/regions")
    fun updateRegion(
        @RequestHeader("userId") userId: String, // TODO: 인증/인가 회복시 @AuthenticationPrincipal 으로 변경
        @Valid @RequestBody addressUpdateRequest: AddressUpdateRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.updateRegion(
            userId = userId,
            requestCommand = addressUpdateRequest.toCommand()
        )

        return ApiResponse.success()
    }

    @PutMapping("/me/active-profile")
    fun updateActiveProfile(
        @RequestHeader("userId") userId: String, // TODO: 인증/인가 회복시 @AuthenticationPrincipal 으로 변경
        @Valid @RequestBody activeProfileUpdateRequest: ActiveProfileUpdateRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.updateActiveProfile(
            userId = userId,
            requestCommand = activeProfileUpdateRequest.toCommand()
        )

        return ApiResponse.success()
    }
}
