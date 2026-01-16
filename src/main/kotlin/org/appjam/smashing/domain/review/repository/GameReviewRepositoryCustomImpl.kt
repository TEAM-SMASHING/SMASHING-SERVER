package org.appjam.smashing.domain.review.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import org.appjam.smashing.domain.review.dto.projection.QUserRecentGameProjection
import org.appjam.smashing.domain.review.dto.projection.UserRecentGameProjection
import org.appjam.smashing.domain.review.entity.QGameReview
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
        activeSportId: Long,
        userId: String,
        snapshotAt: OffsetDateTime
    ): CursorPageResponse<UserRecentGameProjection> {
        val size = request.size.coerceIn(1, 50).toInt()
        val cursor = cursorCodec.decode(request.cursor)
        val gr = QGameReview.gameReview

        val where = BooleanBuilder()
            .and(gr.reviewee.id.eq(userId))
            .and(gr.game.sport.id.eq(activeSportId))
            .and(gr.createdAt.loe(snapshotAt.toLocalDateTime()))

        if (cursor != null) {
            where.and(gr.id.lt(cursor.id))
        }

        val projections = queryFactory
            .select(
                QUserRecentGameProjection(
                    gr.game.id,
                    gr.id,
                    gr.reviewer.nickname,
                    gr.createdAt,
                    gr.content,
                )
            )
            .from(gr)
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
}
