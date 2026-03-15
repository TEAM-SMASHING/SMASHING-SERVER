package org.appjam.smashing.domain.user.repository

import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.appjam.smashing.domain.review.entity.QGameReview
import org.appjam.smashing.domain.user.dto.projection.OtherUserRecommendationProjection
import org.appjam.smashing.domain.user.dto.projection.OtherUserSearchProjection
import org.appjam.smashing.domain.user.dto.projection.QOtherUserRecommendationProjection
import org.appjam.smashing.domain.user.dto.projection.QOtherUserSearchProjection
import org.appjam.smashing.domain.user.entity.QUser.Companion.user
import org.appjam.smashing.domain.user.entity.QUserSportProfile.Companion.userSportProfile
import org.appjam.smashing.global.util.CursorCodec
import org.appjam.smashing.global.util.QueryUtils.randomOrder

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
        limit: Long
    ): List<OtherUserRecommendationProjection> {
        val gr = QGameReview("gr")

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
                user.region.eq(region),
                userSportProfile.sport.id.eq(sportId),
                user.id.ne(excludeUserId),
                userSportProfile.lp.between(myLp - lpThreshold, myLp + lpThreshold)
            )
            .orderBy(randomOrder.asc())
            .limit(limit)
            .fetch()
    }

    override fun findAllBySportOrderByNickname(
        nickname: String,
        sportId: Long,
        excludeUserId: String,
    ): List<OtherUserSearchProjection> =
        queryFactory
            .select(
                QOtherUserSearchProjection(
                    user.id,
                    user.nickname
                )
            ).from(userSportProfile)
            .join(userSportProfile.user, user)
            .where(
                userSportProfile.sport.id.eq(sportId),
                user.id.ne(excludeUserId),
                user.nickname.startsWith(nickname)
            )
            .orderBy(user.nickname.asc())
            .limit(5)
            .fetch()

//    override fun findAllBySportAndRegion(
//        userId: String,
//        sportId: Long,
//        region: String,
//        request: CommonCursorRequest,
//        gender: Gender?,
//        tier: String?,
//        snapshotAt: OffsetDateTime,
//    ): CursorPageResponse<OtherUserRegionProjection> {
//        val size = request.size.coerceIn(1, 50).toInt()
//        val cursor = cursorCodec.decode(request.cursor, OtherUserRegionCursor::class.java)
//
//        val snapshotLocal = snapshotAt
//            .atZoneSameInstant(TimeUtils.DEFAULT_ZONE_ID)
//            .toLocalDateTime()
//
//        val totalGamesExpression = userSportProfile.wins.add(userSportProfile.losses)
//            .castToNum(Long::class.javaObjectType)
//
//        val reviewCountExpression = JPAExpressions
//            .select(gameReview.count())
//            .from(gameReview)
//            .where(
//                gameReview.reviewee.id.eq(user.id),
//                gameReview.game.sport.id.eq(sportId),
//            )
//        val reviewCountOrderExpression = Expressions.numberTemplate(
//            Long::class.javaObjectType,
//            "({0})",
//            reviewCountExpression
//        )
//
//        val where = BooleanBuilder()
//            .and(user.region.eq(region))
//            .and(userSportProfile.sport.id.eq(sportId))
//            .and(user.id.ne(userId))
//            .and(user.createdAt.loe(snapshotLocal))
//
//        gender?.let { where.and(user.gender.eq(gender)) }
//        tier?.let { where.and(userSportProfile.tier.name.startsWithIgnoreCase(tier)) }
//
//        cursor?.let { lastCursor ->
//            val cursorWhere =
//                reviewCountOrderExpression.lt(lastCursor.reviewCount)
//                    .or(
//                        reviewCountOrderExpression.eq(lastCursor.reviewCount)
//                            .and(totalGamesExpression.lt(lastCursor.totalGames))
//                    )
//                    .or(
//                        reviewCountOrderExpression.eq(lastCursor.reviewCount)
//                            .and(totalGamesExpression.eq(lastCursor.totalGames))
//                            .and(user.nickname.gt(lastCursor.nickname))
//                    )
//            where.and(cursorWhere)
//        }
//
//        val projections = queryFactory
//            .select(
//                QOtherUserRegionProjection(
//                    user.id,
//                    user.nickname,
//                    user.gender.stringValue(),
//                    userSportProfile.tier.code,
//                    userSportProfile.wins,
//                    userSportProfile.losses,
//                    reviewCountExpression,
//                )
//            )
//            .from(userSportProfile)
//            .join(userSportProfile.user, user)
//            .where(where)
//            .orderBy(
//                reviewCountOrderExpression.desc(),
//                totalGamesExpression.desc(),
//                user.nickname.asc(),
//            )
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
