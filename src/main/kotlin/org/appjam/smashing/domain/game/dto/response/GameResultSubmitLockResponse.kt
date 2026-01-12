package org.appjam.smashing.domain.game.dto.response

import org.appjam.smashing.global.util.TimeUtils
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

data class GameResultSubmitLockResponse(
    val submitAvailableAt: OffsetDateTime,
    val remainingSeconds: Long,
    val isLocked: Boolean,
) {
    companion object {
        fun from(
            now: LocalDateTime,
            availableAt: LocalDateTime,
        ): GameResultSubmitLockResponse {
            val locked = now.isBefore(availableAt)
            val remainingSeconds = if (locked) ChronoUnit.SECONDS.between(now, availableAt) else 0L

            return GameResultSubmitLockResponse(
                submitAvailableAt = TimeUtils.toOffsetDateTime(availableAt),
                remainingSeconds = remainingSeconds,
                isLocked = locked,
            )
        }
    }
}
