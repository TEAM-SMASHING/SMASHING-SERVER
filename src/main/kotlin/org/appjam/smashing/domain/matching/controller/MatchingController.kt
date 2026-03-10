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
            - 종목은 receiverProfile의 sport로 결정됩니다.
            - 동일 종목/동일 상대 하루 3판 제한: "오늘 RESULT_CONFIRMED 게임 수" 기준
            - 24시간 쿨다운: REQUESTED(createdAt), CANCELLED/REJECTED(respondedAt) 기준
            - 성공 시:
              1) 상대 유저에게 Notification 저장(실시간 반영 X)
              2) SSE: receiver에게 matching.received, requester에게 matching.sent
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
        summary = "보낸 매칭 요청 취소 API",
        description = """
            보낸 매칭 요청을 취소합니다.
            - REQUESTED 상태에서만 취소 가능
            - 취소 시 status=CANCELLED + respondedAt 기록
            - soft delete 처리
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

    @Operation(
        summary = "매칭 요청 거절 API",
        description = """
        수신자가 받은 매칭 요청을 거절합니다.
        - REQUESTED 상태에서만 거절 가능
        - 거절 시 soft delete 처리
        - SSE(matching.updated: REJECTED) 를
          1) receiver(거절한 사람)에게 전송 → 받은 매칭 탭 카드 제거
          2) requester(요청 보낸 사람)에게 전송 → 보낸 매칭 탭 카드 제거
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
        summary = "매칭 요청 수락 API",
        description = """
        받은 매칭 요청을 수락합니다.
        - receiver만 수락 가능
        - REQUESTED 상태에서만 수락 가능
        - 수락 시 매칭이 성사되며 Game이 생성됩니다.
        - 성공 시:
          1) 상대방(requester)에게 MATCHING_ACCEPTED 알림 저장. (실시간 반영 X)
          2) SSE: 상대방(requester)에게 matching.updated(ACCEPTED) SSE를 전송
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
}
