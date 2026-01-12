package org.appjam.smashing.domain.review.service

import org.appjam.smashing.domain.game.repository.GameRepository
import org.appjam.smashing.domain.review.dto.response.ReviewDetailResponse
import org.appjam.smashing.domain.review.entity.GameReview
import org.appjam.smashing.domain.review.enums.ReviewRating
import org.appjam.smashing.domain.review.enums.ReviewTag
import org.appjam.smashing.domain.review.repository.GameReviewRepository
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GameReviewService(
    private val gameRepository: GameRepository,
    private val gameReviewRepository: GameReviewRepository,
) {

    @Transactional
    fun createReview(
        gameId: String,
        reviewer: User,
        reviewee: User,
        rating: ReviewRating,
        content: String?,
        tags: Set<ReviewTag>?,
    ): GameReview {
        val game = gameRepository.findByIdOrNull(gameId)
            ?: throw CustomException(ErrorCode.GAME_NOT_FOUND)

        val review = GameReview.create(
            game = game,
            reviewer = reviewer,
            reviewee = reviewee,
            rating = rating,
            content = content,
        )

        review.tags.addAll(tags.orEmpty())

        return gameReviewRepository.save(review)
    }

    @Transactional(readOnly = true)
    fun getReviewDetail(
        reviewId: String,
    ): ReviewDetailResponse {
        val review = gameReviewRepository.findByIdFetchAll(reviewId)
            ?: throw CustomException(ErrorCode.REVIEW_NOT_FOUND)

        return ReviewDetailResponse.from(review)
    }
}
