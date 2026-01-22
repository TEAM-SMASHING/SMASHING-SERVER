package org.appjam.smashing.domain.user.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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

@Tag(name = "User")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @Operation(
        summary = "닉네임 중복 확인 API",
        description = "닉네임이 중복되어 있는지 확인합니다."
    )
    @GetMapping("/nickname-availability")
    fun checkNicknameAvailability(
        @RequestParam("nickname") nickname: String,
    ): ResponseEntity<ApiResponse<NicknameCheckResponse>> {
        val response = userService.checkNicknameAvailability(nickname)

        return ApiResponse.success(
            data = response
        )
    }

    @Operation(
        summary = "오픈 채팅 링크 확인 API",
        description = "오픈 채팅 링크가 유효한지 확인합니다."
    )
    @PostMapping("/openchat/validate")
    fun validateOpenChatUrl(
        @Valid @RequestBody openChatValidateRequest: OpenChatValidateRequest,
    ): ResponseEntity<ApiResponse<OpenChatValidateResponse>> {
        val response = userService.validateOpenChatUrl(openChatValidateRequest.toCommand())

        return ApiResponse.success(
            data = response
        )
    }

    @Operation(
        summary = "사용자 프로필별 티어 정보 조회 API",
        description = """
           홈 화면에서 유저의 프로필을 티어 정보와 함께 조회합니다.
           - 스포츠 프로필 활성화 여부 상관 없이 allProfiles 에 모든 스포츠를 가나다 순 정렬
           - 활성 여부는 isActive로 판단
        """
    )
    @GetMapping("/me/profiles/tier")
    fun getUserProfileTier(
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<ApiResponse<UserProfileTierResponse>> {
        val response = userService.getUserProfileTier(principal.username)

        return ApiResponse.success(
            data = response
        )
    }

    @Operation(
        summary = "사용자 프로필 추가 API",
        description = "사용자 프로필을 추가합니다."
    )
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

    @Operation(
        summary = "사용자 프로필별 마이페이지 정보 조회 API",
        description = """
            마이페이지 화면에서 유저의 프로필을 티어 정보와 함께 조회합니다.
            - 스포츠 프로필 활성화 여부 상관 없이 allProfiles 에 모든 스포츠를 가나다 순 정렬
           - 활성 여부는 isActive로 판단
        """
    )
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

    @Operation(
        summary = "다른 유저 정보 상세 조회 API",
        description = """
           다른 유저의 프로필을 티어 정보와 함께 조회합니다.
           
           [조건]
           - 상대의 다른 스포츠 프로필을 누르더라도 canChallenge와 canAccept는 나의 스포츠 활성화된 프로필과 동일한 것만 노출
           -선택 여부는 isSelected로 판단
           - QueryParam(sportCode)이 없을 경우 selectedProfile에는 나를 기준으로 활성화된 스포츠 프로필이 디폴트
           
           [정렬]
           - 스포츠 프로필 선택 여부 상관 없이 allProfiles에 모든 스포츠를 가나다 순 정렬
        """
    )
    @GetMapping("/{userId}/profiles")
    fun getOtherUserProfiles(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable userId: String,
        @RequestParam sportCode: String?,
    ): ResponseEntity<ApiResponse<OtherUserProfilesResponse>> {
        val response = userService.getOtherUserProfiles(
            userId = principal.username,
            otherUserId = userId,
            sportCode = sportCode,
        )

        return ApiResponse.success(
            data = response,
        )
    }

    @Operation(
        summary = "사용자 주소 변경 API",
        description = "사용자의 주소를 변경합니다."
    )
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

    @Operation(
        summary = "내 활성 프로필 전환 API ",
        description = "유저의 활성 프로필을 전환합니다."
    )
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

    @Operation(
        summary = "사용자 지역 유저들 추천 조회 API",
        description = """
            주변 추천 유저들을 조회합니다.
            
            [조건]
            - 유저와 지역이랑 활성화된 스포츠가 동일
            
            [정렬]
            - id순으로 정렬 (가입순)
            - lp ±200  이내에서 랜덤
            - 5명 보다 적을 경우 그대로 노출
        """
    )
    @GetMapping("/me/regions/recommendation")
    fun getOtherUsersRecommendation(
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<ApiResponse<OtherUsersRecommendationResponse>> {
        val response = userService.getOtherUsersRecommendation(principal.username)

        return ApiResponse.success(
            data = response,
        )
    }

    @Operation(
        summary = "사용자 지역 유저들 실력순 조회 API",
        description = """
            유저의 지역에 해당되는 유저들을 실력순으로 조회합니다.
            
            [조건]
            - 유저와 지역이랑 활성화된 스포츠가 동일
            
            [정렬]
            - 동네의 상위 30명까지만 노출
            - lp 순 → 닉네임 순
        """
    )
    @GetMapping("/me/regions/leaderboard")
    fun getOtherUsersLeaderBoard(
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<ApiResponse<OtherUsersLeaderBoardResponse>> {
        val response = userService.getOtherUsersLeaderBoard(principal.username)

        return ApiResponse.success(
            data = response
        )
    }

    @Operation(
        summary = "유저 닉네임 기준 목록 검색 API",
        description = """
            다른 유저의 닉네임으로 목록을 검색합니다.
            
            [조건]
            - 유저와 활성화된 스포츠가 동일
            - 유저의 지역과는 무관
            
            [정렬]
            - 최대 5개 노출
        """
    )
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

    @Operation(
        summary = "나의 최근 리뷰 목록 조회 API",
        description = """
            유저가 받은 최근 리뷰 목록을 조회합니다.
            
            [조건]
            - 유저의 지역과는 무관
            - 유저의 활성화된 스포츠 경기에 대한 목록 리뷰
            
            [정렬]
            - 가장 최근에 받은 리뷰 목록 순
        """
    )
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

    @Operation(
        summary = "나의 최근 리뷰 통계 조회 API ",
        description = """
            유저가 받은 리뷰의 통계를 조회합니다.
            
            [조건]
            - 유저의 지역과는 무관
            - 유저의 활성화된 스포츠 경기에 대한 목록 리뷰
        """
    )
    @GetMapping("/me/reviews/summary")
    fun getUserRecentReviewSummary(
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<ApiResponse<UserRecentReviewSummaryResponse>> {
        val response = userService.getUserRecentReviewSummary(principal.username)

        return ApiResponse.success(
            data = response,
        )
    }

    @Operation(
        summary = "유저의 최근 리뷰 목록 조회 API",
        description = """
            다른 유저의 최근 리뷰 목록을 조회합니다.
            
            [조건]
            - 다른 유저의 지역과는 무관
            - 사용자가 선택한 다른 유저의 스포츠 프로필이 노출
            - QueryParam(sportCode)이 없을 경우 나를 기준으로 활성화된 스포츠 프로필이 디폴트
            
            [정렬]
            - 가장 최근에 받은 리뷰 목록 순
        """
    )
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

    @Operation(
        summary = "유저의 최근 리뷰 통계 조회 API",
        description = """
            다른 유저가 받은 리뷰의 통계를 조회합니다.
            
            [조건]
            - 다른 유저의 지역과는 무관
            - 사용자가 선택한 다른 유저의 스포츠 경기에 대한 목록 리뷰
            - QueryParam(sportCode)이 없을 경우 나를 기준으로 활성화된 스포츠 프로필이 디폴트
        """
    )
    @GetMapping("/{userId}/reviews/summary")
    fun getOtherUserRecentReviewSummary(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable userId: String,
        @RequestParam sportCode: String?,
    ): ResponseEntity<ApiResponse<UserRecentReviewSummaryResponse>> {
        val response = userService.getOtherUserRecentReviewSummary(
            userId = principal.username,
            otherUserId = userId,
            sportCode = sportCode,
        )

        return ApiResponse.success(
            data = response,
        )
    }

    @Operation(
        summary = "사용자 지역 유저들 목록 조회 API",
        description = """
            매칭 탐색 화면에서 다른 유저들을 조회합니다.
            
            [조건]
            - 유저의 활성화 되어 있는 스포츠와 동일한 다른 유저 노출
            - 유저의 지역과 동일한 다른 유저 노출
            - QueryParam(gender)가 없으면 전체 성별
            - QueryParam(tier)가 없으면 전체 티어
            
            [정렬]
            - 1순위 후기 개수
            - 2순위 진행한 경기 회수
            - 3순위 닉네임 순
        """
    )
    @GetMapping("/me/regions/users")
    fun getOtherUserRegion(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid regionRequest: OtherUserRegionRequest,
        @Valid requestCursor: CommonCursorRequest,
    ): ResponseEntity<ApiResponse<CursorResponse<OtherUserRegionResponse>>> {
        val response = userService.getOtherUserRegion(
            userId = principal.username,
            requestCommand = regionRequest.toCommand(),
            requestCursor = requestCursor,
        )

        return ApiResponse.success(
            data = response,
        )
    }

    @Operation(
        summary = "사용자 주소 조회 API",
        description = """
            사용자 주소를 조회합니다.
        """
    )
    @GetMapping("/me/regions")
    fun getUserRegion(
        @AuthenticationPrincipal principal: CustomUserDetails,
    ): ResponseEntity<ApiResponse<UserRegionResponse>> {
        val response = userService.getUserRegion(
            userId = principal.username,
        )

        return ApiResponse.success(
            data = response,
        )
    }
}
