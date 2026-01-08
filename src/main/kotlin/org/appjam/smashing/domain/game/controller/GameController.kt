package org.appjam.smashing.domain.game.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
}
