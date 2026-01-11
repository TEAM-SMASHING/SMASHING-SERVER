package org.appjam.smashing.domain.game.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.appjam.smashing.domain.game.dto.response.PendingResultAcceptedGameSummaryResponse
import org.appjam.smashing.domain.game.service.GameService
import org.appjam.smashing.global.common.dto.ApiResponse
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Game")
@RestController
@RequestMapping("/api/v1/games")
class GameQueryController(
    private val gameService: GameService,
) {

    @Operation(
        summary = "결과 확정 전(수락된) 게임 목록 조회 API",
        description = """
            매칭 수락(ACCEPTED)되어 게임이 자동 생성된 이후, 아직 결과가 확정(RESULT_CONFIRMED)되지 않은 게임 목록을 조회합니다.

            - 조회 대상(resultStatus):
              - PENDING_RESULT / WAITING_CONFIRMATION / RESULT_REJECTED (CONFIRMED, CANCELED 제외)

            - 기준 스포츠:
              - 로그인 유저의 활성 프로필(activeUserSportProfileId)의 sport

            - 정렬:
              - gameId(TSID) DESC

            - 페이징:
              - snapshotAt 이전 데이터만 조회(스냅샷 고정)
              - cursor = gameId (마지막 gameId)

            - 잠금(결과 제출 가능 시각) 계산:
              - 목록은 전체(스냅샷 이전) 조회
              - 잠금 시간 계산만 '오늘 기준 정책'으로 계산해 내려줌
        """
    )
    @GetMapping("/accepted/pending-result")
    fun getPendingResultAcceptedGames(
        @RequestHeader("userId") userId: String, // TODO: 인증/인가 적용 시 변경
        @Valid request: CommonCursorRequest,
    ): ResponseEntity<ApiResponse<CursorResponse<PendingResultAcceptedGameSummaryResponse>>> {
        val response = gameService.getPendingResultAcceptedGames(
            userId = userId,
            request = request,
        )

        return ApiResponse.success(response)
    }
}
