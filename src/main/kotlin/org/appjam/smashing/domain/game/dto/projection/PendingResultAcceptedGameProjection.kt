package org.appjam.smashing.domain.game.dto.projection

import com.querydsl.core.annotations.QueryProjection
import org.appjam.smashing.domain.game.enums.GameResultStatus
import org.appjam.smashing.domain.user.enums.Gender
import org.appjam.smashing.global.common.dto.CursorKey
import org.appjam.smashing.global.util.TimeUtils
import java.time.LocalDateTime
import java.time.OffsetDateTime

@QueryProjection
data class PendingResultAcceptedGameProjection(
    val gameId: String,
    val createdAtLdt: LocalDateTime,
    val resultStatus: GameResultStatus,
    val requesterUserId: String,
    val receiverUserId: String,
    val opponentUserId: String,
    val opponentNickname: String,
    val opponentOpenchatUrl: String?,
    val opponentGender: Gender,
    val opponentTierId: Long,
    val opponentTierName: String,
) : CursorKey {

    override val cursorId: String
        get() = gameId

    val createdAt: OffsetDateTime
        get() = TimeUtils.toOffsetDateTime(createdAtLdt)
}
