package org.appjam.smashing.domain.review.repository

import org.appjam.smashing.domain.review.dto.projection.UserRecentGameProjection
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import java.time.OffsetDateTime

interface GameReviewRepositoryCustom {
//    /**
//     * 유저의 최근 경기 목록을 조회합니다.
//     *
//     * [필터조건]
//     * - 기준 유저의 활성화된 스포츠 경기 목록
//     * - 지역 무관
//     *
//     * [정렬]
//     * - 최신 게임 순
//     */
//    fun findAllBySportIdOrderByDate(
//        request: CommonCursorRequest,
//        sportId: Long,
//        userId: String,
//        snapshotAt: OffsetDateTime
//    ): CursorPageResponse<UserRecentGameProjection>
}
