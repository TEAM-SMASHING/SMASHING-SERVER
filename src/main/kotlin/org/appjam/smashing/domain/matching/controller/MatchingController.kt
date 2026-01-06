package org.appjam.smashing.domain.matching.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.appjam.smashing.domain.matching.service.MatchingService
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
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
            - 동일 유저 간 하루 최대 3회 신청 가능합니다.
            - 성공 시 상대 유저에게 알림 및 SSE 이벤트가 전송됩니다.
        """
    )
    @PostMapping("/profiles/{receiverProfileId}")
    fun requestMatching(
        // @AuthenticationPrincipal principal: CustomUserDetails, TODO: 인증/인가 회복시 주석 해제
        @RequestHeader("userId") requesterUserId: String,
        @PathVariable receiverProfileId: String,
    ): ResponseEntity<ApiResponse<Unit>> {
        matchingService.requestMatching(
            // requesterUserId = principal.username, TODO: 인증/인가 회복시 주석 해제
            requesterUserId = requesterUserId,
            receiverProfileId = receiverProfileId,
        )

        return ApiResponse.success()
    }
}
