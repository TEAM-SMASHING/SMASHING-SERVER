package org.appjam.smashing.domain.matching.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.appjam.smashing.domain.matching.dto.projection.QReceivedMatchingSummaryProjection
import org.appjam.smashing.domain.matching.entity.QMatching.Companion.matching
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.appjam.smashing.domain.review.entity.QGameReview.Companion.gameReview
import org.appjam.smashing.domain.tier.entity.QTier
import org.appjam.smashing.domain.user.entity.QUser
import org.appjam.smashing.domain.user.entity.QUserSportProfile
import org.appjam.smashing.domain.matching.dto.projection.ReceivedMatchingSummaryProjection
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import org.appjam.smashing.global.util.CursorCodec
import org.appjam.smashing.global.util.TimeUtils
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
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

        val requester = QUser("requester")
        val requesterProfile = QUserSportProfile("requesterProfile")
        val requesterTier = QTier("requesterTier")

        val where = BooleanBuilder()
            .and(matching.receiver.id.eq(receiverUserId))
            .and(matching.sport.id.eq(sportId))
            .and(matching.status.eq(MatchingStatus.REQUESTED))
            .and(matching.createdAt.loe(snapshotLocal))

        if (cursor != null) {
            where.and(matching.id.lt(cursor.id))
        }

        // requester(상대)가 reviewee인 후기 개수
        val requesterReviewCount = JPAExpressions
            .select(gameReview.count())
            .from(gameReview)
            .where(gameReview.reviewee.id.eq(requester.id))

        val projections = queryFactory
            .select(
                QReceivedMatchingSummaryProjection(
                    matching.id,
                    matching.createdAt,
                    matching.status,
                    requester.id,
                    requester.nickname,
                    requester.gender,
                    requesterReviewCount,
                    requesterTier.id,
                    requesterTier.name,
                    requesterProfile.wins,
                    requesterProfile.losses,
                )
            )
            .from(matching)
            .join(matching.requester, requester)
            .join(requesterProfile).on(
                requesterProfile.user.eq(requester)
                    .and(requesterProfile.sport.id.eq(sportId))
            )
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
}
