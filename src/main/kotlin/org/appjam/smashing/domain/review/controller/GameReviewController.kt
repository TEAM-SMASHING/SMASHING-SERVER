package org.appjam.smashing.domain.review.controller

import org.appjam.smashing.domain.review.service.GameReviewService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reviews")
class GameReviewController(
    private val gameReviewService: GameReviewService,
) {
    fun getReview() {

    }
}