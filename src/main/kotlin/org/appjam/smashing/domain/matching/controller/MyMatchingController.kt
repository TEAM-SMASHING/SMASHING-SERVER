package org.appjam.smashing.domain.matching.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.appjam.smashing.domain.matching.service.MatchingService
import org.appjam.smashing.domain.matching.dto.response.ReceivedMatchingSummaryResponse
import org.appjam.smashing.domain.matching.dto.response.SentMatchingSummaryResponse
import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.appjam.smashing.global.common.dto.ApiResponse
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Matching")
@RestController
@RequestMapping("/api/v1/users/me/matchings")
class MyMatchingController(
    private val matchingService: MatchingService,
) {

    @Operation(
        summary = "내가 받은 매칭 요청 목록 조회 API",
        description = """
            내가 받은 매칭 요청(REQUESTED 상태) 목록을 조회합니다.

            - 정렬 기준
             - 최신순: matching.id DESC (TSID 기반 최신순)
            - 페이징 방식
             - cursor(keyset) 기반 페이징
             - nextCursor는 "마지막 matching id"로 생성.
             - 다음 페이지 요청 시 cursor에 nextCursor를 전달.
            - 스냅샷 고정
             - 최초 요청에서 snapshotAt이 없으면 서버가 now로 고정.
             - 다음 페이지 요청부터는 응답의 snapshotAt을 그대로 재전송.
        """
    )
    @GetMapping("/received")
    fun getReceivedMatchings(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid request: CommonCursorRequest,
    ): ResponseEntity<ApiResponse<CursorResponse<ReceivedMatchingSummaryResponse>>> {
        val response = matchingService.getReceivedMatchings(
            userId = principal.username,
            request = request,
        )

        return ApiResponse.success(response)
    }

    @Operation(
        summary = "내가 보낸 매칭 요청 목록 조회 API",
        description = """
            내가 보낸 매칭 요청(REQUESTED 상태) 목록을 조회합니다.
            - 정렬 기준
             - 최신순: matching.id DESC (TSID 기반 최신순)
            - 페이징 방식
             - cursor(keyset) 기반 페이징
             - nextCursor는 "마지막 matching id"로 생성.
             - 다음 페이지 요청 시 cursor에 nextCursor를 전달.
            - 스냅샷 고정
             - 최초 요청에서 snapshotAt이 없으면 서버가 now로 고정.
             - 다음 페이지 요청부터는 응답의 snapshotAt을 그대로 재전송.
            - 상대 정보
             - receiver(수신자) 정보 제공
             - gender(MALE/FEMALE) 포함
        """
    )
    @GetMapping("/sent")
    fun getSentMatchings(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid request: CommonCursorRequest,
    ): ResponseEntity<ApiResponse<CursorResponse<SentMatchingSummaryResponse>>> {
        val response = matchingService.getSentMatchings(
            userId = principal.username,
            request = request,
        )

        return ApiResponse.success(response)
    }
}
