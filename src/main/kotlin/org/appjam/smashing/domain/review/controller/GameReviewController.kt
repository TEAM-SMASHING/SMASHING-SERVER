package org.appjam.smashing.domain.review.controller

import org.appjam.smashing.domain.review.dto.response.ReviewDetailResponse
import org.appjam.smashing.domain.review.service.GameReviewService
import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reviews")
class GameReviewController(
    private val gameReviewService: GameReviewService,
) {
    @GetMapping("/{reviewId}")
    fun getReviewDetail(
        @AuthenticationPrincipal principal: CustomUserDetails,
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
