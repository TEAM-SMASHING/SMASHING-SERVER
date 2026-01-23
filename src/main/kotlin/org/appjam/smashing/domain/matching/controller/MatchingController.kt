package org.appjam.smashing.domain.matching.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.appjam.smashing.domain.matching.service.MatchingService
import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Matching")
@RestController
@RequestMapping("/api/v1/matchings")
class MatchingController(
    private val matchingService: MatchingService,
) {

    @Operation(
        summary = "매칭 신청 API",
        description = """
            상대 스포츠 프로필(receiverProfileId)에 대해 매칭을 신청합니다.
            - 인증된 사용자 기준으로 매칭 요청을 생성합니다.
            - 동일 유저 간 하루 최대 3회 신청 가능합니다. (앱잼 제외)
            - 상대방에게 24시간 이내 결과까지 모두 확정되지 않은 이력이 존재할 경우 신청 불가합니다.
            - 성공 시 상대 유저에게 알림 및 SSE 이벤트가 전송됩니다.
        """
    )
    @PostMapping("/profiles/{receiverProfileId}")
    fun requestMatching(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable receiverProfileId: String,
    ): ResponseEntity<ApiResponse<Unit>> {
        matchingService.requestMatching(
            requesterUserId = principal.username,
            receiverProfileId = receiverProfileId,
        )

        return ApiResponse.success()
    }

    @Operation(
        summary = "매칭 요청 수락 API",
        description = """
            매칭 요청(matchingId)을 수락합니다.
            - 성공 시 상대(requester)에게 알림 생성 + SSE 이벤트가 전송됩니다.
        """
    )
    @PostMapping("/{matchingId}/accept")
    fun acceptMatching(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable matchingId: String,
    ): ResponseEntity<ApiResponse<Unit>> {
        matchingService.acceptMatching(
            receiverUserId = principal.username,
            matchingId = matchingId,
        )

        return ApiResponse.success()
    }

    @Operation(
        summary = "매칭 요청 거절 API",
        description = """
            수신자가 받은 매칭 요청을 거절합니다.
            - REQUESTED 상태에서만 거절 가능
            - 거절 시 soft delete 처리
            - Notification 생성 없음
            - requester에게 SSE(matching.updated: REJECTED) 전송
        """
    )
    @PostMapping("/{matchingId}/reject")
    fun rejectMatching(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable matchingId: String,
    ): ResponseEntity<ApiResponse<Unit>> {
        matchingService.rejectMatching(
            receiverUserId = principal.username,
            matchingId = matchingId,
        )

        return ApiResponse.success()
    }

    @Operation(
        summary = "내가 보낸 매칭 요청 삭제 API",
        description = """
            내가 보낸 매칭 요청을 삭제합니다.
            - REQUESTED 상태에서만 삭제 가능
            - 삭제 시 soft delete 처리
            - Notification 생성 없음
            - receiver에게 SSE(matching.updated: CANCELLED) 전송
        """
    )
    @DeleteMapping("/{matchingId}")
    fun cancelMyMatchingRequest(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable matchingId: String,
    ): ResponseEntity<ApiResponse<Unit>> {
        matchingService.cancelMyMatchingRequest(
            requesterUserId = principal.username,
            matchingId = matchingId,
        )

        return ApiResponse.success()
    }
}
