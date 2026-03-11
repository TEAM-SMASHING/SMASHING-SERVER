package org.appjam.smashing.domain.review.service

import org.appjam.smashing.domain.game.entity.Game
import org.appjam.smashing.domain.game.repository.GameRepository
import org.appjam.smashing.domain.review.dto.response.ReviewDetailResponse
import org.appjam.smashing.domain.review.entity.GameReview
import org.appjam.smashing.domain.review.enums.ReviewRating
import org.appjam.smashing.domain.review.enums.ReviewTag
import org.appjam.smashing.domain.review.repository.GameReviewRepository
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
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

    fun createReview(
        game: Game,
        reviewerProfile: UserSportProfile,
        revieweeProfile: UserSportProfile,
        rating: ReviewRating,
        content: String?,
        tags: Set<ReviewTag>,
    ): GameReview {
        return gameReviewRepository.save(
            GameReview.create(
                game = game,
                reviewerProfile = reviewerProfile,
                revieweeProfile = revieweeProfile,
                rating = rating,
                content = content,
                tags = tags,
            )
        )
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
