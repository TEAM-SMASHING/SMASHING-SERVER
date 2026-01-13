package org.appjam.smashing.domain.review.repository

import org.appjam.smashing.domain.review.entity.GameReview
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface GameReviewRepository : JpaRepository<GameReview, String> {

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

    @Query
        (
        """
            select r.reviewee.id, count(r.id)
            from GameReview r
            where r.game.sport.id = :sportId
              and r.reviewee.id in :userIds
            group by r.reviewee.id
        """
    )
    fun countReviewsBySportAndReviewees(
        sportId: Long,
        userIds: List<String>
    ): List<Array<Any>>
}
