package org.appjam.smashing.domain.matching.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.appjam.smashing.domain.matching.dto.projection.QReceivedMatchingSummaryProjection
import org.appjam.smashing.domain.matching.dto.projection.QSentMatchingSummaryProjection
import org.appjam.smashing.domain.matching.entity.QMatching.Companion.matching
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.appjam.smashing.domain.review.entity.QGameReview.Companion.gameReview
import org.appjam.smashing.domain.tier.entity.QTier
import org.appjam.smashing.domain.user.entity.QUser
import org.appjam.smashing.domain.user.entity.QUserSportProfile
import org.appjam.smashing.domain.matching.dto.projection.ReceivedMatchingSummaryProjection
import org.appjam.smashing.domain.matching.dto.projection.SentMatchingSummaryProjection
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import org.appjam.smashing.global.util.CursorCodec
import org.appjam.smashing.global.util.TimeUtils
import java.time.OffsetDateTime

class MatchingRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
    private val cursorCodec: CursorCodec,
) : MatchingRepositoryCustom {

    override fun fetchReceivedRequestedPage(
        receiverUserId: String,
        sportId: Long,
        request: CommonCursorRequest,
        snapshotAt: OffsetDateTime,
    ): CursorPageResponse<ReceivedMatchingSummaryProjection> {

        val size = request.size.coerceIn(1, 50).toInt()
        val cursor = cursorCodec.decode(request.cursor)

        val snapshotLocal = snapshotAt
            .atZoneSameInstant(TimeUtils.DEFAULT_ZONE_ID)
            .toLocalDateTime()

        val requesterUser = QUser("requesterUser")
        val requesterProfile = QUserSportProfile("requesterProfile")
        val requesterTier = QTier("requesterTier")

        val where = BooleanBuilder()
            .and(matching.receiverProfile.user.id.eq(receiverUserId))
            .and(matching.sport.id.eq(sportId))
            .and(matching.status.eq(MatchingStatus.REQUESTED))
            .and(matching.createdAt.loe(snapshotLocal))

        if (cursor != null) {
            where.and(matching.id.lt(cursor.id))
        }

        // 전체 종목 통합 후기 수 (해당 유저가 받은 전체 후기 수)
        val requesterReviewCount = JPAExpressions
            .select(gameReview.count())
            .from(gameReview)
            .where(gameReview.revieweeProfile.user.id.eq(requesterUser.id))

        val projections = queryFactory
            .select(
                QReceivedMatchingSummaryProjection(
                    matching.id,
                    matching.createdAt,
                    matching.status,
                    requesterUser.id,
                    requesterProfile.id,
                    requesterUser.nickname,
                    requesterUser.gender,
                    requesterReviewCount,
                    requesterTier.code,
                    requesterProfile.wins,
                    requesterProfile.losses,
                )
            )
            .from(matching)
            .join(matching.requesterProfile, requesterProfile)
            .join(requesterProfile.user, requesterUser)
            .join(requesterProfile.tier, requesterTier)
            .where(where)
            .orderBy(matching.id.desc())
            .limit((size + 1).toLong())
            .fetch()

        return CursorPageResponse.create(
            snapshotAt = snapshotAt,
            fetched = projections,
            pageSize = size,
            cursorCodec = cursorCodec,
        )
    }

    override fun fetchSentRequestedPage(
        requesterUserId: String,
        sportId: Long,
        request: CommonCursorRequest,
        snapshotAt: OffsetDateTime,
    ): CursorPageResponse<SentMatchingSummaryProjection> {

        val size = request.size.coerceIn(1, 50).toInt()
        val cursor = cursorCodec.decode(request.cursor)

        val snapshotLocal = snapshotAt
            .atZoneSameInstant(TimeUtils.DEFAULT_ZONE_ID)
            .toLocalDateTime()

        val receiverUser = QUser("receiverUser")
        val receiverProfile = QUserSportProfile("receiverProfile")
        val receiverTier = QTier("receiverTier")

        val where = BooleanBuilder()
            .and(matching.requesterProfile.user.id.eq(requesterUserId))
            .and(matching.sport.id.eq(sportId))
            .and(matching.status.eq(MatchingStatus.REQUESTED))
            .and(matching.createdAt.loe(snapshotLocal))

        if (cursor != null) {
            where.and(matching.id.lt(cursor.id))
        }

        // 전체 종목 통합 후기 수 (해당 유저가 받은 전체 후기 수)
        val receiverReviewCount = JPAExpressions
            .select(gameReview.count())
            .from(gameReview)
            .where(gameReview.revieweeProfile.user.id.eq(receiverUser.id))

        val projections = queryFactory
            .select(
                QSentMatchingSummaryProjection(
                    matching.id,
                    matching.createdAt,
                    matching.status,
                    receiverUser.id,
                    receiverProfile.id,
                    receiverUser.nickname,
                    receiverUser.gender,
                    receiverReviewCount,
                    receiverTier.code,
                    receiverProfile.wins,
                    receiverProfile.losses,
                )
            )
            .from(matching)
            .join(matching.receiverProfile, receiverProfile)
            .join(receiverProfile.user, receiverUser)
            .join(receiverProfile.tier, receiverTier)
            .where(where)
            .orderBy(matching.id.desc())
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
