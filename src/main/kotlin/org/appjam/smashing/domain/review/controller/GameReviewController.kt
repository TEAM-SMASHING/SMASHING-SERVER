package org.appjam.smashing.domain.review.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.appjam.smashing.domain.review.dto.response.ReviewDetailResponse
import org.appjam.smashing.domain.review.service.GameReviewService
import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "GameReview")
@RestController
@RequestMapping("/api/v1/reviews")
class GameReviewController(
    private val gameReviewService: GameReviewService,
) {
    @Operation(
        summary = "후기 단건 상세 조회 API",
        description = "리뷰 후기를 단건 상세 조회합니다."
    )
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
