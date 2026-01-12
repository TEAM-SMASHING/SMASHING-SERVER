package org.appjam.smashing.domain.review.controller

import org.appjam.smashing.domain.review.dto.response.ReviewDetailResponse
import org.appjam.smashing.domain.review.service.GameReviewService
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reviews")
class GameReviewController(
    private val gameReviewService: GameReviewService,
) {
    @GetMapping("/{reviewId}")
    fun getReviewDetail(
        @RequestHeader("userId") userId: String, // TODO: 인증/인가 회복시 @AuthenticationPrincipal 으로 변경
        @PathVariable("reviewId") reviewId: String,
    ): ResponseEntity<ApiResponse<ReviewDetailResponse>> {
        val response = gameReviewService.getReviewDetail(
            reviewId = reviewId,
        )

        return ApiResponse.success(
            data = response
        )
    }
}