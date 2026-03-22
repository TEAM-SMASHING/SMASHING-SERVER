package org.appjam.smashing.domain.user.repository

import org.appjam.smashing.domain.user.dto.projection.OtherUserRecommendationProjection
import org.appjam.smashing.domain.user.dto.projection.OtherUserRegionProjection
import org.appjam.smashing.domain.user.dto.projection.OtherUserSearchProjection
import org.appjam.smashing.domain.user.enums.Gender
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import java.time.OffsetDateTime

interface UserSportProfileRepositoryCustom {
    fun findRandomRecommendation(
        region: String,
        sportId: Long,
        excludeUserId: String,
        myLp: Int,
        lpThreshold: Int,
        limit: Long,
    ): List<OtherUserRecommendationProjection>

    /**
     * nickname을 기준으로 조건에 맞는 유저를 반환합니다.
     *
     * [검색 및 필터 조건]
     * 1) 입력한 nickname으로 시작하는 유저
     * 2) 기준 유저의 활성화된 스포츠와 동일한 스포츠를 가진 유저
     * 3) 지역 무관
     * 4) 본인(excludeUserId) 제외
     *
     * [정렬 및 제한]
     * - 닉네임 오름차순 정렬
     * - 최대 5명까지 반환
     *
     * @param nickname 검색하고자 하는 닉네임
     * @param sportId 유저의 활성화된 스포츠 아이디
     * @param excludeUserId 제외하고자 하는 유저 (본인)
     * @return 조건에 부합하는 다른 유저의 아이디와 닉네임
     */
    fun findAllBySportOrderByNickname(
        nickname: String,
        sportId: Long,
        excludeUserId: String,
    ): List<OtherUserSearchProjection>

    fun findAllBySportAndRegion(
        userId: String,
        sportId: Long,
        region: String,
        request: CommonCursorRequest,
        gender: Gender?,
        tier: String?,
        snapshotAt: OffsetDateTime,
    ): CursorPageResponse<OtherUserRegionProjection>
}
