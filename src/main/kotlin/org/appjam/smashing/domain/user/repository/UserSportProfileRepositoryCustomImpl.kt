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
import org.appjam.smashing.global.util.QueryUtils.randomOrder

class UserSportProfileRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
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
                    userSportProfile.tier.id,
                    userSportProfile.wins,
                    userSportProfile.losses,
                    JPAExpressions
                        .select(gr.count().castToNum(Int::class.java))
                        .from(gr)
                        .where(
                            gr.reviewee.id.eq(user.id),
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
}
