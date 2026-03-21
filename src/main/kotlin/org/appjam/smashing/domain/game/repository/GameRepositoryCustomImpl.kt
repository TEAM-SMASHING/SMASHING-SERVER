package org.appjam.smashing.domain.game.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.appjam.smashing.domain.game.dto.projection.PendingResultAcceptedGameProjection
import org.appjam.smashing.domain.game.dto.projection.QPendingResultAcceptedGameProjection
import org.appjam.smashing.domain.game.entity.QGame.Companion.game
import org.appjam.smashing.domain.game.entity.QGameResultSubmission
import org.appjam.smashing.domain.game.enums.GameStatus
import org.appjam.smashing.domain.matching.entity.QMatching.Companion.matching
import org.appjam.smashing.domain.tier.entity.QTier
import org.appjam.smashing.domain.user.entity.QUser
import org.appjam.smashing.domain.user.entity.QUserSportProfile
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import org.appjam.smashing.global.util.CursorCodec
import org.appjam.smashing.global.util.CursorQueryUtils
import org.appjam.smashing.global.util.TimeUtils
import java.time.OffsetDateTime

class GameRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
    private val cursorCodec: CursorCodec,
) : GameRepositoryCustom {

    override fun fetchPendingResultAcceptedGamesPage(
        userId: String,
        sportId: Long,
        request: CommonCursorRequest,
        snapshotAt: OffsetDateTime,
    ): CursorPageResponse<PendingResultAcceptedGameProjection> {

        val size = request.size.coerceIn(1, 50).toInt()
        val cursor = cursorCodec.decode(request.cursor)
        val orderType = CursorQueryUtils.resolveOrderType(request.order)

        val snapshotLocal = snapshotAt.atZoneSameInstant(TimeUtils.DEFAULT_ZONE_ID)
            .toLocalDateTime()

        val requesterUser = QUser("requesterUser")
        val receiverUser = QUser("receiverUser")
        val requesterProfile = QUserSportProfile("requesterProfile")
        val receiverProfile = QUserSportProfile("receiverProfile")
        val requesterTier = QTier("requesterTier")
        val receiverTier = QTier("receiverTier")

        val submission = QGameResultSubmission("submission")
        val submissionSub = QGameResultSubmission("submissionSub")

        val opponentProfileIdExpr = CaseBuilder()
            .`when`(matching.requesterProfile.user.id.eq(userId)).then(receiverProfile.id)
            .otherwise(requesterProfile.id)

        val opponentNicknameExpr = CaseBuilder()
            .`when`(matching.requesterProfile.user.id.eq(userId)).then(receiverUser.nickname)
            .otherwise(requesterUser.nickname)

        val opponentGenderExpr = CaseBuilder()
            .`when`(matching.requesterProfile.user.id.eq(userId)).then(receiverUser.gender)
            .otherwise(requesterUser.gender)

        val opponentTierCodeExpr = CaseBuilder()
            .`when`(matching.requesterProfile.user.id.eq(userId)).then(receiverTier.code)
            .otherwise(requesterTier.code)

        // 가장 최근 제출안의 attemptNo 서브쿼리
        val latestAttemptNoSubQuery = JPAExpressions
            .select(submissionSub.attemptNo.max())
            .from(submissionSub)
            .where(submissionSub.game.id.eq(game.id))

        val where = BooleanBuilder()
            .and(
                matching.requesterProfile.user.id.eq(userId)
                    .or(matching.receiverProfile.user.id.eq(userId))
            )
            .and(matching.sport.id.eq(sportId))
            .and(game.createdAt.loe(snapshotLocal))
            .and(
                game.resultStatus.`in`(
                    GameStatus.PENDING_RESULT,
                    GameStatus.WAITING_CONFIRMATION,
                    GameStatus.RESULT_REJECTED,
                )
            )

        CursorQueryUtils.cursorWhere(
            path = game.id,
            orderType = orderType,
            cursorValue = cursor?.id,
        )?.let(where::and)

        val projections = queryFactory
            .select(
                QPendingResultAcceptedGameProjection(
                    game.id,
                    game.createdAt,
                    game.resultStatus,
                    matching.requesterProfile.user.id,
                    matching.receiverProfile.user.id,
                    opponentProfileIdExpr,
                    opponentNicknameExpr,
                    opponentGenderExpr,
                    opponentTierCodeExpr,
                    submission.id,
                    submission.attemptNo,
                    submission.submitterProfile.id,
                )
            )
            .from(game)
            .join(game.matching, matching)
            .join(matching.requesterProfile, requesterProfile)
            .join(requesterProfile.user, requesterUser)
            .join(requesterProfile.tier, requesterTier)
            .join(matching.receiverProfile, receiverProfile)
            .join(receiverProfile.user, receiverUser)
            .join(receiverProfile.tier, receiverTier)
            .leftJoin(submission).on(
                submission.game.eq(game)
                    .and(submission.attemptNo.eq(latestAttemptNoSubQuery))
            )
            .where(where)
            .orderBy(CursorQueryUtils.orderBy(game.id, orderType))
            .limit((size + 1).toLong())
            .fetch()

        return CursorPageResponse.create(
            snapshotAt = snapshotAt,
            fetched = projections,
            pageSize = size,
            cursorCodec = cursorCodec,
        )
    }
}
