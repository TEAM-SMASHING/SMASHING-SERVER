package org.appjam.smashing.domain.scheduler

import org.appjam.smashing.domain.game.enums.GameStatus
import org.appjam.smashing.domain.game.repository.GameRepository
import org.appjam.smashing.global.util.TimeUtils
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class GameExpiryScheduler(
    private val gameRepository: GameRepository,
) {
    // 매 10분마다 만료된 게임 처리
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    @Transactional
    fun expireGames() {
        val now = LocalDateTime.now(TimeUtils.DEFAULT_ZONE_ID)

        // 72시간 경과 기준
        val expiredBefore = now.minusHours(72)

        // 만료 대상 상태
        // - PENDING_RESULT: Host가 결과 작성 안 함
        // - WAITING_CONFIRMATION: 상대가 확인 안 함
        // - RESULT_REJECTED: 반려 후 Host가 재작성 안 함
        val expiredStatuses = listOf(
            GameStatus.PENDING_RESULT,
            GameStatus.WAITING_CONFIRMATION,
            GameStatus.RESULT_REJECTED,
        )

        val expiredGames = gameRepository.findAllExpiredGames(
            statuses = expiredStatuses,
            expiredBefore = expiredBefore,
        )

        // 만료 처리 (기록되지 않음)
        expiredGames.forEach { game ->
            game.expire()
        }
    }
}
