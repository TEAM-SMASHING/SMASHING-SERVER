package org.appjam.smashing.domain.review.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.appjam.smashing.domain.review.dto.projection.QUserRecentGameProjection
import org.appjam.smashing.domain.review.dto.projection.UserRecentGameProjection
import org.appjam.smashing.domain.review.dto.response.QReviewRatingCount
import org.appjam.smashing.domain.review.dto.response.ReviewRatingCount
import org.appjam.smashing.domain.review.dto.response.ReviewTagCount
import org.appjam.smashing.domain.review.entity.QGameReview
import org.appjam.smashing.domain.user.entity.QUser
import org.appjam.smashing.domain.user.entity.User.Companion.DELETED_USER_NICKNAME
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import org.appjam.smashing.global.util.CursorCodec
import java.time.OffsetDateTime

class GameReviewRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
    private val cursorCodec: CursorCodec,
) : GameReviewRepositoryCustom {
    override fun findAllBySportIdOrderByDate(
        request: CommonCursorRequest,
        sportId: Long,
        userId: String,
        snapshotAt: OffsetDateTime,
        blockIds: List<String>,
    ): CursorPageResponse<UserRecentGameProjection> {
        val size = request.size.coerceIn(1, 50).toInt()
        val cursor = cursorCodec.decode(request.cursor)
        val gr = QGameReview.gameReview

        val where = BooleanBuilder()
            .and(gr.revieweeProfile.user.id.eq(userId))
            .and(gr.game.sport.id.eq(sportId))
            .and(gr.createdAt.loe(snapshotAt.toLocalDateTime()))
            .and(gr.content.isNotNull)
            .and(gr.content.isNotEmpty)

        if (cursor != null) {
            where.and(gr.id.lt(cursor.id))
        }

        val reviewer = QUser.user

        // 차단 필터링
        if (blockIds.isNotEmpty()) {
            where.and(reviewer.id.isNull.or(reviewer.id.notIn(blockIds)))
        }

        val projections = queryFactory
            .select(
                QUserRecentGameProjection(
                    gr.id,
                    reviewer.nickname.coalesce(DELETED_USER_NICKNAME),
                    gr.createdAt,
                    gr.content,
                )
            )
            .from(gr)
            .leftJoin(gr.reviewerProfile.user, reviewer)
            .where(where)
            .orderBy(gr.id.desc())
            .limit((size + 1).toLong())
            .fetch()

        return CursorPageResponse.create(
            snapshotAt = snapshotAt,
            fetched = projections,
            pageSize = size,
            cursorCodec = cursorCodec,
        )
    }

    override fun countRatingsByRevieweeAndSport(
        revieweeId: String,
        sportId: Long,
        blockIds: List<String>,
    ): List<ReviewRatingCount> {
        val gr = QGameReview.gameReview

        // 차단 필터링
        val blockCondition = if (blockIds.isNotEmpty()) {
            gr.reviewerProfile.user.id.isNull
                .or(gr.reviewerProfile.user.id.notIn(blockIds))
        } else null

        return queryFactory
            .select(
                QReviewRatingCount(
                    gr.rating,
                    gr.id.count()
                )
            )
            .from(gr)
            .leftJoin(gr.reviewerProfile.user)
            .where(
                gr.revieweeProfile.user.id.eq(revieweeId),
                gr.game.sport.id.eq(sportId),
                blockCondition,
            )
            .groupBy(gr.rating)
            .fetch()
    }

    override fun countTagsByRevieweeAndSport(
        revieweeId: String,
        sportId: Long,
        blockIds: List<String>,
    ): List<ReviewTagCount> {
        val gr = QGameReview.gameReview

        // 차단 필터링
        val blockCondition = if (blockIds.isNotEmpty()) {
            gr.reviewerProfile.user.id.isNull
                .or(gr.reviewerProfile.user.id.notIn(blockIds))
        } else null

        val filteredReviews = queryFactory
            .selectFrom(gr)
            .leftJoin(gr.reviewerProfile.user)
            .where(
                gr.revieweeProfile.user.id.eq(revieweeId),
                gr.game.sport.id.eq(sportId),
                blockCondition,
            )
            .fetch()

        return filteredReviews
            .flatMap { it.tags }
            .groupingBy { it }
            .eachCount()
            .map { (tag, count) -> ReviewTagCount(tag, count.toLong()) }
    }
}
