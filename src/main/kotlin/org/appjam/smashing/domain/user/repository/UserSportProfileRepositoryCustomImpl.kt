package org.appjam.smashing.domain.user.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.appjam.smashing.domain.review.entity.QGameReview
import org.appjam.smashing.domain.review.entity.QGameReview.Companion.gameReview
import org.appjam.smashing.domain.user.dto.projection.*
import org.appjam.smashing.domain.user.entity.QUser.Companion.user
import org.appjam.smashing.domain.user.entity.QUserSportProfile.Companion.userSportProfile
import org.appjam.smashing.domain.user.enums.Gender
import org.appjam.smashing.domain.user.enums.UserStatus
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import org.appjam.smashing.global.util.CursorCodec
import org.appjam.smashing.global.util.QueryUtils.randomOrder
import org.appjam.smashing.global.util.TimeUtils
import java.time.LocalDateTime
import java.time.OffsetDateTime

class UserSportProfileRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
    private val cursorCodec: CursorCodec,
) : UserSportProfileRepositoryCustom {
    override fun findRandomRecommendation(
        region: String,
        sportId: Long,
        excludeUserId: String,
        myLp: Int,
        lpThreshold: Int,
        limit: Long,
        blockIds: List<String>,
    ): List<OtherUserRecommendationProjection> {
        val gr = QGameReview("gr")
        val now = LocalDateTime.now()

        return queryFactory
            .select(
                QOtherUserRecommendationProjection(
                    user.id,
                    user.nickname,
                    userSportProfile.tier.code,
                    userSportProfile.wins,
                    userSportProfile.losses,
                    JPAExpressions
                        .select(gr.count().castToNum(Int::class.java))
                        .from(gr)
                        .where(
                            gr.revieweeProfile.user.id.eq(user.id),
                            gr.game.sport.id.eq(sportId)
                        ),
                    user.gender.stringValue(),
                )
            )
            .from(userSportProfile)
            .join(userSportProfile.user, user)
            .where(
                // 기본 필터링
                user.region.eq(region),
                userSportProfile.sport.id.eq(sportId),
                user.id.ne(excludeUserId),
                userSportProfile.lp.between(myLp - lpThreshold, myLp + lpThreshold),
                // 신고 필터링
                user.status.eq(UserStatus.ACTIVE)
                    .or(
                        user.status.eq(UserStatus.RESTRICTED)
                            .and(user.restrictionEndDate.before(now))
                    ),
                // 차단 필터링
                if (blockIds.isNotEmpty()) user.id.notIn(blockIds) else null
            )
            .orderBy(randomOrder.asc())
            .limit(limit)
            .fetch()
    }

    override fun findAllBySportOrderByNickname(
        nickname: String,
        sportId: Long,
        excludeUserId: String,
        blockIds: List<String>,
    ): List<OtherUserSearchProjection> {
        val now = LocalDateTime.now()

        return queryFactory
            .select(
                QOtherUserSearchProjection(
                    user.id,
                    user.nickname
                )
            ).from(userSportProfile)
            .join(userSportProfile.user, user)
            .where(
                // 기본 필터링
                userSportProfile.sport.id.eq(sportId),
                user.id.ne(excludeUserId),
                user.nickname.startsWith(nickname),
                // 신고 필터링
                user.status.eq(UserStatus.ACTIVE)
                    .or(
                        user.status.eq(UserStatus.RESTRICTED)
                            .and(user.restrictionEndDate.before(now))
                    ),
                // 차단 필터링
                if (blockIds.isNotEmpty()) user.id.notIn(blockIds) else null,
            )
            .orderBy(user.nickname.asc())
            .limit(5)
            .fetch()
    }

    override fun findAllBySportAndRegion(
        userId: String,
        sportId: Long,
        region: String,
        request: CommonCursorRequest,
        gender: Gender?,
        tier: String?,
        snapshotAt: OffsetDateTime,
        blockIds: List<String>,
    ): CursorPageResponse<OtherUserRegionProjection> {
        val size = request.size.coerceIn(1, 50).toInt()
        val cursor = cursorCodec.decode(request.cursor, OtherUserRegionCursor::class.java)

        val snapshotLocal = snapshotAt
            .atZoneSameInstant(TimeUtils.DEFAULT_ZONE_ID)
            .toLocalDateTime()

        val totalGamesExpression = userSportProfile.wins.add(userSportProfile.losses)
            .castToNum(Long::class.javaObjectType)

        val reviewCountExpression = JPAExpressions
            .select(gameReview.count())
            .from(gameReview)
            .where(
                gameReview.revieweeProfile.user.id.eq(user.id),
                gameReview.game.sport.id.eq(sportId),
            )
        val reviewCountOrderExpression = Expressions.numberTemplate(
            Long::class.javaObjectType,
            "({0})",
            reviewCountExpression
        )

        val where = BooleanBuilder()
            .and(user.region.eq(region))
            .and(userSportProfile.sport.id.eq(sportId))
            .and(user.id.ne(userId))
            .and(user.createdAt.loe(snapshotLocal))

        // 차단 관계 유저 필터링
        if (blockIds.isNotEmpty()) {
            where.and(user.id.notIn(blockIds))
        }

        gender?.let { where.and(user.gender.eq(gender)) }
        tier?.let { where.and(userSportProfile.tier.name.startsWithIgnoreCase(tier)) }

        cursor?.let { lastCursor ->
            val cursorWhere =
                reviewCountOrderExpression.lt(lastCursor.reviewCount)
                    .or(
                        reviewCountOrderExpression.eq(lastCursor.reviewCount)
                            .and(totalGamesExpression.lt(lastCursor.totalGames))
                    )
                    .or(
                        reviewCountOrderExpression.eq(lastCursor.reviewCount)
                            .and(totalGamesExpression.eq(lastCursor.totalGames))
                            .and(user.nickname.gt(lastCursor.nickname))
                    )
            where.and(cursorWhere)
        }

        val projections = queryFactory
            .select(
                QOtherUserRegionProjection(
                    user.id,
                    user.nickname,
                    user.gender.stringValue(),
                    userSportProfile.tier.code,
                    userSportProfile.wins,
                    userSportProfile.losses,
                    reviewCountExpression,
                )
            )
            .from(userSportProfile)
            .join(userSportProfile.user, user)
            .where(where)
            .orderBy(
                reviewCountOrderExpression.desc(),
                totalGamesExpression.desc(),
                user.nickname.asc(),
            )
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
