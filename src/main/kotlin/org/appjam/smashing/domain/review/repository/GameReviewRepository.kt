package org.appjam.smashing.domain.review.repository

import org.appjam.smashing.domain.review.dto.response.ReviewRatingCount
import org.appjam.smashing.domain.review.dto.response.ReviewTagCount
import org.appjam.smashing.domain.review.entity.GameReview
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GameReviewRepository : JpaRepository<GameReview, String>, GameReviewRepositoryCustom {

    //        @Query(
//        """
//        select count(gr)
//          from GameReview gr
//          join gr.game g
//         where gr.reviewee.id = :revieweeUserId
//           and g.sport.id = :sportId
//        """
//    )
//    fun countByRevieweeAndSport(
//        revieweeUserId: String,
//        sportId: Long,
//    ): Long
//
    @Query(
        """
            select gr
            from GameReview gr
            left join fetch gr.reviewerProfile rp
            left join fetch rp.user
            left join fetch gr.revieweeProfile ep
            left join fetch ep.user
            left join fetch gr.tags
            where gr.id = :reviewId
        """
    )
    fun findByIdFetchAll(
        @Param("reviewId") reviewId: String,
    ): GameReview?

    @Query(
        """
            select gr.rating as reviewRating,
            count(gr.id) as counts
            from GameReview gr
            where gr.revieweeProfile.user.id = :revieweeId
            and gr.game.sport.id = :sportId
            group by gr.rating
        """
    )
    fun countRatingsByRevieweeAndSport(
        revieweeId: String,
        sportId: Long
    ): List<ReviewRatingCount>

    @Query(
        """
            select t as reviewTag,
            count(t) as counts
            from GameReview gr
            join gr.tags t
            where gr.revieweeProfile.user.id = :revieweeId
            and gr.game.sport.id = :sportId
            group by t
        """
    )
    fun countTagsByRevieweeAndSport(
        revieweeId: String,
        sportId: Long
    ): List<ReviewTagCount>

//    @Query(
//        """
//    select gr.id
//    from GameReview gr
//    where gr.game.id = :gameId
//      and gr.reviewer.id = :reviewerId
//      and gr.reviewee.id = :revieweeId
//"""
//    )
//    fun findIdByGameAndReviewerAndReviewee(
//        gameId: String,
//        reviewerId: String,
//        revieweeId: String,
//    ): String?
}
