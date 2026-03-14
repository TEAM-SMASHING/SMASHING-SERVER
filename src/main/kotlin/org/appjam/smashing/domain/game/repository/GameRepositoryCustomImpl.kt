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
import org.appjam.smashing.domain.matching.enums.MatchingStatus
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

//    override fun fetchPendingResultAcceptedGamesPage(
//        userId: String,
//        sportId: Long,
//        request: CommonCursorRequest,
//        snapshotAt: OffsetDateTime,
//    ): CursorPageResponse<PendingResultAcceptedGameProjection> {
//
//        val size = request.size.coerceIn(1, 50).toInt()
//        val cursor = cursorCodec.decode(request.cursor)
//        val orderType = CursorQueryUtils.resolveOrderType(request.order)
//
//        val snapshotLocal = snapshotAt
//            .atZoneSameInstant(TimeUtils.DEFAULT_ZONE_ID)
//            .toLocalDateTime()
//
//        val requester = QUser("requester")
//        val receiver = QUser("receiver")
//        val requesterProfile = QUserSportProfile("requesterProfile")
//        val receiverProfile = QUserSportProfile("receiverProfile")
//        val requesterTier = QTier("requesterTier")
//        val receiverTier = QTier("receiverTier")
//
//        val submission = QGameResultSubmission("submission")
//        val submissionSub = QGameResultSubmission("submissionSub")
//
//        val opponentIdExpr = CaseBuilder().`when`(matching.requester.id.eq(userId)).then(receiver.id)
//            .otherwise(requester.id)
//
//        val opponentNicknameExpr = CaseBuilder().`when`(matching.requester.id.eq(userId)).then(receiver.nickname)
//            .otherwise(requester.nickname)
//
//        val opponentOpenchatExpr = CaseBuilder().`when`(matching.requester.id.eq(userId)).then(receiver.openchatUrl)
//            .otherwise(requester.openchatUrl)
//
//        val opponentGenderExpr = CaseBuilder().`when`(matching.requester.id.eq(userId)).then(receiver.gender)
//            .otherwise(requester.gender)
//
//        val opponentTierCodeExpr = CaseBuilder().`when`(matching.requester.id.eq(userId)).then(receiverTier.code)
//            .otherwise(requesterTier.code)
//
//        val latestAttemptNoSubQuery = JPAExpressions.select(submissionSub.attemptNo.max())
//            .from(submissionSub)
//            .where(submissionSub.game.id.eq(game.id))
//
//        val where = BooleanBuilder().and(
//                matching.requester.id.eq(userId)
//                    .or(matching.receiver.id.eq(userId))
//            )
//            .and(matching.sport.id.eq(sportId))
//            .and(matching.status.eq(MatchingStatus.ACCEPTED))
//            .and(game.createdAt.loe(snapshotLocal))
//            .and(
//                game.resultStatus.`in`(
//                    GameStatus.PENDING_RESULT,
//                    GameStatus.WAITING_CONFIRMATION,
//                    GameStatus.RESULT_REJECTED,
//                    GameStatus.CANCELED
//                )
//            )
//
//        CursorQueryUtils.cursorWhere(
//            path = game.id,
//            orderType = orderType,
//            cursorValue = cursor?.id,
//        )?.let(where::and)
//
//        val projections = queryFactory
//            .select(
//                QPendingResultAcceptedGameProjection(
//                    game.id,
//                    game.createdAt,
//                    game.resultStatus,
//                    matching.requester.id,
//                    matching.receiver.id,
//                    opponentIdExpr,
//                    opponentNicknameExpr,
//                    opponentOpenchatExpr,
//                    opponentGenderExpr,
//                    opponentTierCodeExpr,
//                    submission.id,
//                    submission.attemptNo,
//                    submission.submitter.id
//                )
//            )
//            .from(game)
//            .join(game.matching, matching)
//            .join(matching.requester, requester)
//            .join(matching.receiver, receiver)
//            .join(requesterProfile).on(
//                requesterProfile.user.eq(requester)
//                    .and(requesterProfile.sport.id.eq(sportId))
//            )
//            .join(requesterProfile.tier, requesterTier)
//            .join(receiverProfile).on(
//                receiverProfile.user.eq(receiver)
//                    .and(receiverProfile.sport.id.eq(sportId))
//            )
//            .join(receiverProfile.tier, receiverTier)
//            .leftJoin(submission).on(
//                submission.game.eq(game)
//                    .and(submission.attemptNo.eq(latestAttemptNoSubQuery))
//            )
//            .where(where)
//            .orderBy(CursorQueryUtils.orderBy(game.id, orderType))
//            .limit((size + 1).toLong())
//            .fetch()
//
//        return CursorPageResponse.create(
//            snapshotAt = snapshotAt,
//            fetched = projections,
//            pageSize = size,
//            cursorCodec = cursorCodec,
//        )
//    }
}
