package org.appjam.smashing.domain.user.repository

import org.appjam.smashing.domain.user.dto.projection.OtherUserRecommendationProjection
import org.appjam.smashing.domain.user.dto.projection.OtherUserRegionProjection
import org.appjam.smashing.domain.user.dto.projection.OtherUserSearchProjection
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.domain.user.enums.Gender
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorPageResponse
import java.time.OffsetDateTime

interface UserSportProfileRepositoryCustom {
    /**
     * [추가 제재]
     * - 신고로 인해 정지당한 유저는 제외
     * - 차단한 유저는 상호 제외
     */
    fun findRandomRecommendation(
        region: String,
        sportId: Long,
        excludeUserId: String,
        myLp: Int,
        lpThreshold: Int,
        limit: Long,
        blockIds: List<String>,
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
     * [추가 제재]
     * - 신고로 인해 정지당한 유저는 제외
     * - 차단한 유저는 서로 제외
     *
     * @param nickname 검색하고자 하는 닉네임
     * @param sportId 유저의 활성화된 스포츠 아이디
     * @param excludeUserId 제외하고자 하는 유저 (본인)
     * @param blockIds 차단 제재 적용
     * @return 조건에 부합하는 다른 유저의 아이디와 닉네임
     */
    fun findAllBySportOrderByNickname(
        nickname: String,
        sportId: Long,
        excludeUserId: String,
        blockIds: List<String>
    ): List<OtherUserSearchProjection>

    fun findAllBySportAndRegion(
        userId: String,
        sportId: Long,
        region: String,
        request: CommonCursorRequest,
        gender: Gender?,
        tier: String?,
        snapshotAt: OffsetDateTime,
        blockIds: List<String>,
    ): CursorPageResponse<OtherUserRegionProjection>

    /**
     * [추가 제재]
     * - 신고로 인해 정지당한 유저는 제외
     * - 차단한 유저는 상호 제외
     */
    fun findAllByRegionAndSportOrderByLp(
        region: String,
        sportId: Long,
    ): List<UserSportProfile>
}
