package org.appjam.smashing.domain.game.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.appjam.smashing.domain.game.dto.request.GameResultConfirmRequest
import org.appjam.smashing.domain.game.dto.request.GameResultRejectRequest
import org.appjam.smashing.domain.game.dto.request.GameResultSubmitRequest
import org.appjam.smashing.domain.game.dto.response.GameResultConfirmResponse
import org.appjam.smashing.domain.game.dto.response.GameResultSubmissionDetailResponse
import org.appjam.smashing.domain.game.dto.response.GameResultSubmitResponse
import org.appjam.smashing.domain.game.dto.response.PendingResultAcceptedGameSummaryResponse
import org.appjam.smashing.domain.game.service.GameService
import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.appjam.smashing.global.common.dto.ApiResponse
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Game")
@RestController
@RequestMapping("/api/v1/games")
class GameController(
    private val gameService: GameService,
) {

    @Operation(
        summary = "경기 결과 제출/재제출 API",
        description = """
            경기 결과를 제출하거나, 반려된 결과를 1회 재제출합니다.
            - Host(매칭의 receiverProfile)만 결과 작성 가능
            - 승패는 profileId 기준으로 입력
            - 최초 제출: 경기 생성 후 첫 경기면 1시간, 같은 날 연속 경기면 조건부 10분 동안 제출 불가
            - 재제출: 경기가 RESULT_REJECTED 상태에서만 가능
            - 결과는 최대 2회(최초 제출 1회 + 재제출 1회)까지 허용
            - 성공 시 상대방에게 game.updated SSE가 발행.
        """
    )
    @PostMapping("/{gameId}/submissions")
    fun submitGameResult(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable gameId: String,
        @Valid @RequestBody request: GameResultSubmitRequest,
    ): ResponseEntity<ApiResponse<GameResultSubmitResponse>> {
        val response = gameService.submitResult(
            submitterUserId = principal.username,
            gameId = gameId,
            command = request.toCommand(),
        )

        return ApiResponse.success(response)
    }

    @Operation(
        summary = "경기 결과 반려/재반려 API",
        description = """
        상대가 제출한 경기 결과를 반려합니다.
        - 결과 확인자(confirmer)만 반려할 수 있습니다.
        - 1차 반려 시 반려 사유는 필수이며, Host에게 결과 재작성 기회가 1회 주어집니다.
        - 2차 반려 시 반려 사유 없이 반려하며, 해당 경기는 기록되지 않음 처리되어 취소됩니다.
        - 성공 시 상대방에게 game.updated SSE가 발행.
    """
    )
    @PostMapping("/{gameId}/submissions/{submissionId}/reject")
    fun rejectGameResult(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable gameId: String,
        @PathVariable submissionId: String,
        @Valid @RequestBody request: GameResultRejectRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        gameService.rejectResult(
            confirmerUserId = principal.username,
            gameId = gameId,
            submissionId = submissionId,
            command = request.toCommand(),
        )

        return ApiResponse.success()
    }

    @Operation(
        summary = "경기 결과 수락/확정 API",
        description = """
            상대가 제출한 경기 결과를 수락(확정)합니다.
            - 결과 확인자(confirmer)만 승인할 수 있습니다.
            - 1차 제출 승인 / 2차 재제출 승인 모두 동일한 API로 처리합니다.
            - 승인 시 경기 결과가 최종 확정되며, 승자 +30 LP / 패자 -20 LP(최소 0)가 반영됩니다.
            - LP 반영 후 티어를 재산정합니다.
            - confirmer가 host에게 작성한 리뷰가 저장되고, host에게 후기 도착 알림이 생성됩니다.
            - 성공 시 host에게 game.updated SSE가 발행됩니다.
        """
    )
    @PostMapping("/{gameId}/submissions/{submissionId}/confirm")
    fun confirmGameResult(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable gameId: String,
        @PathVariable submissionId: String,
        @Valid @RequestBody request: GameResultConfirmRequest,
    ): ResponseEntity<ApiResponse<GameResultConfirmResponse>> {
        val response = gameService.confirmResult(
            confirmerUserId = principal.username,
            gameId = gameId,
            submissionId = submissionId,
            command = request.toCommand(),
        )

        return ApiResponse.success(response)
    }

    @Operation(
        summary = "경기 결과 제출안 단건 조회 API",
        description = """
        경기 결과 제출안(submission) 단건을 조회합니다.
        - 제출 회차(attemptNo), 제출자, 제출안 기준 승자/패자를 조회합니다.
    """
    )
    @GetMapping("/{gameId}/submissions/{submissionId}")
    fun getSubmissionDetail(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable gameId: String,
        @PathVariable submissionId: String,
    ): ResponseEntity<ApiResponse<GameResultSubmissionDetailResponse>> {
        val response = gameService.getSubmissionDetail(
            gameId = gameId,
            submissionId = submissionId,
        )

        return ApiResponse.success(response)
    }

    @Operation(
        summary = "결과 확정 전(수락된) 게임 목록 조회 API",
        description = """
            매칭 수락(ACCEPTED)되어 게임이 자동 생성된 이후, 아직 결과가 확정(RESULT_CONFIRMED)되지 않은 게임 목록을 조회합니다.

            - 조회 대상(resultStatus):
              - PENDING_RESULT / WAITING_CONFIRMATION / RESULT_REJECTED (CONFIRMED, CANCELED 제외)

            - 기준 스포츠:
              - 로그인 유저의 활성 프로필(activeUserSportProfileId)의 sport

            - 정렬 : order 파라미터 기반 정렬
              - LATEST / latest / Latest 등 대소문자 무관
              - OLDEST / oldest / Oldest 등 대소문자 무관
              - 기본값(default) : order 미지정 또는 잘못된 값 → LATEST 적용
              - 정렬 기준 :
                - LATEST : gameId (TSID) DESC
                - OLDEST : gameId (TSID) ASC

            - 페이징:
              - snapshotAt 이전 데이터만 조회(스냅샷 고정)
              - cursor = gameId (마지막 gameId)

            - 잠금(결과 제출 가능 시각) 계산:
              - 목록은 전체(스냅샷 이전) 조회
              - 잠금 시간 계산만 '오늘 기준 정책'으로 계산해 내려줌
        """
    )
    @GetMapping("/pending-results")
    fun getPendingResultAcceptedGames(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid request: CommonCursorRequest,
    ): ResponseEntity<ApiResponse<CursorResponse<PendingResultAcceptedGameSummaryResponse>>> {
        val response = gameService.getPendingResultAcceptedGames(
            userId = principal.username,
            request = request,
        )

        return ApiResponse.success(response)
    }
}
