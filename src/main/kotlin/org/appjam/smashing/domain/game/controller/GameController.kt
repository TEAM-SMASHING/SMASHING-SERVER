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
import org.appjam.smashing.domain.game.service.GameService
import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.appjam.smashing.global.common.dto.ApiResponse
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
            - Host(requester)만 결과 작성 가능
            - 승패는 profileId 기준으로 입력
            - 최초 제출: 경기 생성 후 첫 경기면 1시간, 같은 날 연속 경기면 조건부 10분 동안 제출 불가
            - 재제출: 경기가 RESULT_REJECTED 상태에서만 가능
            - 결과는 최대 2회(최초 제출 1회 + 재제출 1회)까지 허용
            - 성공 시 상대방에게 game.updated SSE가 발행.
        """
    )
    @PostMapping("/{gameId}/submissions")
    fun submitGameResult(
        @RequestHeader principal: String,
        @PathVariable gameId: String,
        @Valid @RequestBody request: GameResultSubmitRequest,
    ): ResponseEntity<ApiResponse<GameResultSubmitResponse>> {
        val response = gameService.submitResult(
            submitterUserId = principal,
            gameId = gameId,
            command = request.toCommand(),
        )

        return ApiResponse.success(response)
    }

//    @Operation(
//        summary = "경기 결과 수락/확정 API",
//        description = """
//            상대가 제출한 경기 결과를 수락(확정)합니다.
//
//            [정책]
//            - game.resultStatus 는 WAITING_CONFIRMATION 이어야 합니다.
//            - submission.status 는 SUBMITTED 이어야 합니다.
//            - confirmerUserId 는 해당 submission.confirmer 와 일치해야 합니다.
//
//            [확정 시 처리]
//            - Game.resultStatus = RESULT_CONFIRMED 로 변경
//            - Game.scoreWinner/scoreLoser/confirmedAt/winner/loser/confirmedSubmissionId 세팅
//            - GameResultSubmission.status = ACCEPTED, actedAt 세팅
//            - 승/패에 따라 양쪽 UserSportProfile.wins/losses, lp 업데이트
//              - 총 경기수(기존 wins+losses 기준) 1~3판: 승 +90 / 패 -20
//              - 4~8판: 승 +45 / 패 -15
//              - 9판~: 승 +30 / 패 -20
//              - lp 는 0 미만으로 내려가지 않음
//            - lp 변경 후 Tier 테이블(minLp/maxLp) 기준으로 tier 갱신
//
//            [SSE/알림]
//            - game.updated SSE: 상대에게 발송
//            - review 포함 시:
//              - 리뷰 저장(확정자 -> 제출자)
//              - REVIEW_RECEIVED 알림 + review.received.notification.created SSE (제출자에게)
//        """
//    )
//    @PostMapping("/{gameId}/submissions/{submissionId}/confirm")
//    fun confirmGameResult(
//        @AuthenticationPrincipal principal: CustomUserDetails,
//        @PathVariable gameId: String,
//        @PathVariable submissionId: String,
//        @Valid @RequestBody request: GameResultConfirmRequest,
//    ): ResponseEntity<ApiResponse<GameResultConfirmResponse>> {
//        val response =gameService.confirmResult(
//            confirmerUserId = principal.username,
//            gameId = gameId,
//            submissionId = submissionId,
//            command = request.toCommand(),
//        )
//
//        return ApiResponse.success(response)
//    }
//
//    @Operation(
//        summary = "경기 결과 제출안 단건 조회 API",
//        description = """
//            경기 결과 제출안(submission) 단건을 조회합니다.
//            - 제출 회차(attemptNo) / 제출자(submitter) / 제출안 기준 승자/패자 + 점수 반환
//        """
//    )
//    @GetMapping("/{gameId}/submissions/{submissionId}")
//    fun getSubmissionDetail(
//        @AuthenticationPrincipal principal: CustomUserDetails,
//        @PathVariable gameId: String,
//        @PathVariable submissionId: String,
//    ): ResponseEntity<ApiResponse<GameResultSubmissionDetailResponse>> {
//        val response = gameService.getSubmissionDetail(
//            gameId = gameId,
//            submissionId = submissionId,
//        )
//
//        return ApiResponse.success(response)
//    }

    @Operation(
        summary = "경기 결과 거절 API",
        description = """
            상대가 제출한 경기 결과 제출안(submission)을 거절합니다.
            - 거절은 submission.confirmer(받은 사람)만 가능합니다.
            - 상태 변경 sse가 발행됩니다.
            - 거절 사유에 따라 NotificationType이 분리되어 전송됩니다. (알림+sse)
        """
    )
    @PostMapping("/{gameId}/submissions/{submissionId}/reject")
    fun rejectGameResult(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable gameId: String,
        @PathVariable submissionId: String,
        @Valid @RequestBody request: GameResultRejectRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
//        gameService.rejectResult(
//            confirmerUserId = principal.username,
//            gameId = gameId,
//            submissionId = submissionId,
//            command = request.toCommand(),
//        )

        return ApiResponse.success()
    }

    @Operation(
        summary = "경기 삭제 API",
        description = """
            경기를 삭제(soft delete)합니다.
            - 게임의 resultStatus를 CANCELED로 변경.
            - 앱잼 기간 내 삭제 x. canceled로만 변경.
            - 단, RESULT_CONFIRMED(확정) 상태면 삭제 불가
            - 삭제 시 제출안(GameResultSubmission)도 함께 soft delete 처리.
            - 경기 참여자(매칭 requester/receiver)만 삭제 가능
            - 게임 상태 변경 SSE(game.updated)가 상대에게 발행. (resultStatus = CANCELED)
        """
    )
    @PutMapping("/{gameId}")
    fun deleteGame(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable gameId: String,
    ): ResponseEntity<ApiResponse<Unit>> {
        gameService.deleteGame(
            userId = principal.username,
            gameId = gameId,
        )

        return ApiResponse.success()
    }
}
