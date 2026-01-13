package org.appjam.smashing.domain.user.repository

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.appjam.smashing.domain.user.entity.QUser.Companion.user
import org.appjam.smashing.domain.user.entity.QUserSportProfile.Companion.userSportProfile
import org.appjam.smashing.domain.user.entity.UserSportProfile

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
    ): List<UserSportProfile> =
        queryFactory
            .selectFrom(userSportProfile)
            .join(
                userSportProfile.user,
                user,
            ).fetchJoin()
            .where(
                user.region.eq(region),
                userSportProfile.sport.id.eq(sportId),
                user.id.ne(excludeUserId),
                userSportProfile.lp.between(myLp - lpThreshold, myLp + lpThreshold)
            )
            .orderBy(Expressions.numberTemplate(Double::class.java, "function('RAND')").asc())
            .limit(limit)
            .fetch()
}
