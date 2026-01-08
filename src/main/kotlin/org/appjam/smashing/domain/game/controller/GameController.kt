package org.appjam.smashing.domain.game.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.appjam.smashing.domain.game.dto.request.GameResultConfirmRequest
import org.appjam.smashing.domain.game.dto.request.GameResultSubmitRequest
import org.appjam.smashing.domain.game.service.GameService
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Game")
@RestController
@RequestMapping("/api/v1/games")
class GameController(
    private val gameService: GameService,
) {

    @Operation(
        summary = "경기 결과 제출 API",
        description = """
            경기 결과를 제출합니다.
            - 경기 생성 후 1시간 동안 제출 불가
            - 동일 유저와 전체 2~3번째 경기인데 첫 경기로부터 30분 이내에 잡힌 경기면, 해당 경기는 생성 후 10분 동안 제출 불가
            - review는 최초 제출(attemptNo=1)에서만 허용 (attemptNo=1이면 review 필수)
            - 제출 성공 시 상대에게 결과 확인 알림 + 알림 SSE 전송
            - review 포함이면 +(후기 알림 + 알림 SSE 전송)
            - 게임 상태 WAITING_CONFIRMATION 전환 이후 SSE는 상대에게 전송
        """
    )
    @PostMapping("/{gameId}/submissions")
    fun submitGameResult(
        @RequestHeader("userId") submitterUserId: String, // TODO: 인증/인가 적용시 변경
        @PathVariable gameId: String,
        @Valid @RequestBody request: GameResultSubmitRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        gameService.submitResult(
            submitterUserId = submitterUserId,
            gameId = gameId,
            command = request.toCommand(),
        )

        return ApiResponse.success()
    }

    @Operation(
        summary = "경기 결과 수락/확정 API",
        description = """
            상대가 제출한 경기 결과를 수락(확정)합니다.

            [정책]
            - game.resultStatus 는 WAITING_CONFIRMATION 이어야 합니다.
            - submission.status 는 SUBMITTED 이어야 합니다.
            - confirmerUserId 는 해당 submission.confirmer 와 일치해야 합니다.
            - review 정책은 제출 API와 동일:
              - attemptNo=1 인 제출 건을 확정할 때만 review 허용(+필수)
              - attemptNo!=1 인 제출 건은 review 불가

            [확정 시 처리]
            - Game.resultStatus = RESULT_CONFIRMED 로 변경
            - Game.scoreWinner/scoreLoser/confirmedAt/winner/loser/confirmedSubmissionId 세팅
            - GameResultSubmission.status = ACCEPTED, actedAt 세팅
            - 승/패에 따라 양쪽 UserSportProfile.wins/losses, lp 업데이트
              - 총 경기수(기존 wins+losses 기준) 1~3판: 승 +90 / 패 -20
              - 4~8판: 승 +45 / 패 -15
              - 9판~: 승 +30 / 패 -20
              - lp 는 0 미만으로 내려가지 않음
            - lp 변경 후 Tier 테이블(minLp/maxLp) 기준으로 tier 갱신

            [SSE/알림]
            - game.updated SSE: 상대에게 발송
            - review 포함 시:
              - 리뷰 저장(확정자 -> 제출자)
              - REVIEW_RECEIVED 알림 + review.received.notification.created SSE (제출자에게)
        """
    )
    @PostMapping("/{gameId}/submissions/{submissionId}/confirm")
    fun confirmGameResult(
        @RequestHeader("userId") confirmerUserId: String, // TODO: 인증/인가 적용시 변경
        @PathVariable gameId: String,
        @PathVariable submissionId: String,
        @Valid @RequestBody request: GameResultConfirmRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        gameService.confirmResult(
            confirmerUserId = confirmerUserId,
            gameId = gameId,
            submissionId = submissionId,
            command = request.toCommand(),
        )

        return ApiResponse.success()
    }
}
