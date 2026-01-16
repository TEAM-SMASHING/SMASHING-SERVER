package org.appjam.smashing.domain.review.repository

import org.appjam.smashing.domain.review.entity.GameReview
import org.appjam.smashing.domain.review.enums.ReviewRating
import org.appjam.smashing.domain.review.enums.ReviewTag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReviewRatingCount {
    val reviewRating: ReviewRating
    val counts: Long
}

interface ReviewTagCount {
    val reviewTag: ReviewTag
    val counts: Long
}

interface GameReviewRepository : JpaRepository<GameReview, String>, GameReviewRepositoryCustom {

    @Query(
        """
        select count(gr)
          from GameReview gr
          join gr.game g
         where gr.reviewee.id = :revieweeUserId
           and g.sport.id = :sportId
        """
    )
    fun countByRevieweeAndSport(
        revieweeUserId: String,
        sportId: Long,
    ): Long

    @Query(
        """
            select gr
            from GameReview gr
            join fetch gr.reviewer
            join fetch gr.reviewee
            left join fetch gr.tags
            where gr.id = :reviewId
        """
    )
    fun findByIdFetchAll(
        reviewId: String,
    ): GameReview?

    @Query(
        """
            select gr.rating, count(gr)
            from GameReview gr
            where gr.reviewee.id = :revieweeId
            and gr.game.sport.id = :activeSportId
            group by gr.rating
        """
    )
    fun countRatingsByRevieweeAndSport(
        revieweeId: String,
        activeSportId: Long
    ): List<ReviewRatingCount>

    @Query(
        """
            select t, count(t)
            from GameReview gr
            join gr.tags t
            where gr.reviewee.id = :revieweeId
            and gr.game.sport.id = :activeSportId
            group by t
        """
    )
    fun countTagsByRevieweeAndSport(
        revieweeId: String,
        activeSportId: Long
    ): List<ReviewTagCount>
}
