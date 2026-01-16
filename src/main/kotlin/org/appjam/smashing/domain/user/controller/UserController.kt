package org.appjam.smashing.domain.user.controller

import jakarta.validation.Valid
import org.appjam.smashing.domain.user.dto.request.*
import org.appjam.smashing.domain.user.dto.response.*
import org.appjam.smashing.domain.user.service.UserService
import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.appjam.smashing.global.common.dto.ApiResponse
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorResponse
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
        @Valid @RequestBody openChatValidateRequest: OpenChatValidateRequest,
    ): ResponseEntity<ApiResponse<OpenChatValidateResponse>> {
        val response = userService.validateOpenChatUrl(openChatValidateRequest.toCommand())

        return ApiResponse.success(
            data = response
        )
    }

    @GetMapping("/me/profiles/tier")
    fun getUserProfileTier(
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<ApiResponse<UserProfileTierResponse>> {
        val response = userService.getUserProfileTier(principal.username)

        return ApiResponse.success(
            data = response
        )
    }

    @PostMapping("/me/profiles")
    fun addProfile(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid @RequestBody profileAddRequest: ProfileAddRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.addProfile(
            userId = principal.username,
            requestCommand = profileAddRequest.toCommand()
        )

        return ApiResponse.success()
    }

    @GetMapping("/me/profiles")
    fun getUserProfiles(
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<ApiResponse<UserProfilesResponse>> {
        val response = userService.getUserProfiles(
            userId = principal.username,
        )

        return ApiResponse.success(
            data = response,
        )
    }

    @GetMapping("/{userId}/profiles")
    fun getOtherUserProfiles(
        @AuthenticationPrincipal principal: CustomUserDetails,
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
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid @RequestBody addressUpdateRequest: AddressUpdateRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.updateRegion(
            userId = principal.username,
            requestCommand = addressUpdateRequest.toCommand()
        )

        return ApiResponse.success()
    }

    @PutMapping("/me/active-profile")
    fun updateActiveProfile(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid @RequestBody activeProfileUpdateRequest: ActiveProfileUpdateRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        userService.updateActiveProfile(
            userId = principal.username,
            requestCommand = activeProfileUpdateRequest.toCommand()
        )

        return ApiResponse.success()
    }

    @GetMapping("/regions/recommendation")
    fun getOtherUsersRecommendation(
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<ApiResponse<OtherUsersRecommendationResponse>> {
        val response = userService.getOtherUsersRecommendation(principal.username)

        return ApiResponse.success(
            data = response,
        )
    }

    @GetMapping("/me/regions/leaderboard")
    fun getOtherUsersLeaderBoard(
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<ApiResponse<OtherUsersLeaderBoardResponse>> {
        val response = userService.getOtherUsersLeaderBoard(principal.username)

        return ApiResponse.success(
            data = response
        )
    }

    @GetMapping("/search")
    fun getOtherUserSearch(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid request: OtherUserSearchRequest,
    ): ResponseEntity<ApiResponse<OtherUserSearchResponse>> {
        val response = userService.getOtherUserSearch(
            userId = principal.username,
            requestCommand = request.toCommand(),
        )

        return ApiResponse.success(
            data = response,
        )
    }

    @GetMapping("/me/reviews/recent")
    fun getUserRecentReview(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid request: CommonCursorRequest,
    ): ResponseEntity<ApiResponse<CursorResponse<UserRecentReviewResponse>>> {
        val response = userService.getUserRecentReview(
            userId = principal.username,
            request = request,
        )

        return ApiResponse.success(
            data = response,
        )
    }

    @GetMapping("/me/reviews/summary")
    fun getUserRecentReviewSummary(
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<ApiResponse<UserRecentReviewSummaryResponse>> {
        val response = userService.getUserRecentReviewSummary(principal.username)

        return ApiResponse.success(
            data = response,
        )
    }

    @GetMapping("/{userId}/reviews/recent")
    fun getOtherUserRecentReview(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable userId: String,
        @RequestParam sportCode: String?,
        @Valid request: CommonCursorRequest,
    ): ResponseEntity<ApiResponse<CursorResponse<UserRecentReviewResponse>>> {
        val response = userService.getOtherUserRecentReview(
            userId = principal.username,
            otherUserId = userId,
            sportCode = sportCode,
            request = request,
        )

        return ApiResponse.success(
            data = response,
        )
    }

    @GetMapping("/{userId}/reviews/summary")
    fun getOtherUserRecentReviewSummary(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable userId: String,
    ): ResponseEntity<ApiResponse<UserRecentReviewSummaryResponse>> {
        val response = userService.getOtherUserRecentReviewSummary(
            userId = principal.username,
            otherUserId = userId,
        )

        return ApiResponse.success(
            data = response,
        )
    }


    @GetMapping("/me/regions/users")
    fun getOtherUserRegion(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid requestDto: OtherUserRegionRequest,
        @Valid requestCursor: CommonCursorRequest,
    ): ResponseEntity<ApiResponse<CursorResponse<OtherUserRegionResponse>>> {
        val response = userService.getOtherUserRegion(
            userId = principal.username,
            requestCommand = requestDto.toCommand(),
            requestCursor = requestCursor,
        )

        return ApiResponse.success(
            data = response,
        )
    }
}
