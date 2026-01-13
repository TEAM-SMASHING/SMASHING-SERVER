package org.appjam.smashing.domain.user.repository

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.appjam.smashing.domain.user.dto.projection.OtherUserRecommendationProjection
import org.appjam.smashing.domain.user.dto.projection.QOtherUserRecommendationProjection
import org.appjam.smashing.domain.user.entity.QUser.Companion.user
import org.appjam.smashing.domain.user.entity.QUserSportProfile.Companion.userSportProfile

class UserSportProfileRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : UserSportProfileRepositoryCustom {
    override fun findRandomCandidates(
        region: String,
        sportId: Long,
        excludeUserId: String,
        myLp: Int,
        lpThreshold: Int,
        limit: Long
    ): List<OtherUserRecommendationProjection> {

        val projection = queryFactory
            .select(
                QOtherUserRecommendationProjection(
                    user.id,
                    user.nickname,
                    userSportProfile.tier.id,
                    userSportProfile.wins,
                    userSportProfile.losses,
                    Expressions.asNumber(0).`as`("reviews"), // reviews는 여기서 0으로 초기화
                    user.gender.stringValue()
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
            .orderBy(Expressions.numberTemplate(Double::class.java, "function('RAND')").asc())
            .limit(limit)
            .fetch()

        return OtherUserRecommendationProjection.create()
    }
}
