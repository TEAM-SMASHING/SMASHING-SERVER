package org.appjam.smashing.domain.user.service

import org.appjam.smashing.domain.game.repository.GameRepository
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.appjam.smashing.domain.matching.repository.MatchingRepository
import org.appjam.smashing.domain.review.repository.GameReviewRepository
import org.appjam.smashing.domain.sport.repository.SportRepository
import org.appjam.smashing.domain.tier.repository.TierRepository
import org.appjam.smashing.domain.user.dto.command.*
import org.appjam.smashing.domain.user.dto.response.*
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.domain.user.repository.UserRepository
import org.appjam.smashing.domain.user.repository.UserSportProfileRepository
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorResponse
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.appjam.smashing.global.util.TimeUtils
import org.appjam.smashing.global.util.TimeUtils.DEFAULT_ZONE_ID
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userSportProfileRepository: UserSportProfileRepository,
    private val sportRepository: SportRepository,
    private val tierRepository: TierRepository,
    private val gameReviewRepository: GameReviewRepository,
    private val gameRepository: GameRepository,
    private val matchingRepository: MatchingRepository,
) {
    @Transactional(readOnly = true)
    fun checkNicknameAvailability(
        nickname: String,
    ): NicknameCheckResponse {
        val trimmedNickname = nickname.trim()

        validateNickName(trimmedNickname)

        return if (userRepository.existsByNickname(trimmedNickname)) {
            NicknameCheckResponse(false)
        } else {
            NicknameCheckResponse(true)
        }
    }

    private fun validateNickName(trimmedNickname: String) {
        if (trimmedNickname.length > MAX_NICKNAME_LENGTH) {
            throw CustomException(ErrorCode.NICKNAME_TOO_LONG)
        }

        if (!NICKNAME_VALID_REGEX.matches(trimmedNickname)) {
            throw CustomException(ErrorCode.INVALID_NICKNAME_FORMAT)
        }
    }

    @Transactional
    fun validateOpenChatUrl(
        openChatValidateCommand: OpenChatValidateCommand,
    ): OpenChatValidateResponse {
        val trimmedUrl = openChatValidateCommand.openchatUrl.trim()

        checkDuplicateOpenChatUrl(trimmedUrl)

        return if (OPEN_CHAT_URL_REGEX.matches(trimmedUrl)) {
            OpenChatValidateResponse(true)
        } else {
            OpenChatValidateResponse(false)
        }
    }

    private fun checkDuplicateOpenChatUrl(trimmedUrl: String) {
        if (userRepository.existsByOpenchatUrl(trimmedUrl)) {
            throw CustomException(ErrorCode.DUPLICATE_OPEN_CHAT_URL)
        }
    }

    @Transactional(readOnly = true)
    fun getUserProfileTier(
        userId: String,
    ): UserProfileTierResponse {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val allProfiles = userSportProfileRepository.findAllByUserIdOrderBySportName(userId)

        val activeProfile = allProfiles.find { it.id == user.activeUserSportProfileId }
            ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)

        return UserProfileTierResponse.from(
            region = user.region,
            nickname = user.nickname,
            activeProfile = activeProfile,
            allProfiles = allProfiles,
        )
    }

    @Transactional
    fun addProfile(
        userId: String,
        requestCommand: ProfileAddCommand,
    ) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val sport = sportRepository.findByCode(requestCommand.sportCode)
            ?: throw CustomException(ErrorCode.SPORT_NOT_FOUND)

        validateAlreadyRegisteredSport(user.id!!, sport.id!!)

        val initLp = requestCommand.experienceRange.initLp
        val initTier = tierRepository.findBySportIdAndLpInRange(
            sportId = sport.id!!,
            lp = initLp,
        ) ?: throw CustomException(ErrorCode.INVALID_INITIAL_TIER)

        val tier = tierRepository.findBySportIdAndName(
            sportId = sport.id!!,
            name = initTier.name,
        ) ?: throw CustomException(ErrorCode.INVALID_TIER_SETTING)

        val profile = userSportProfileRepository.save(
            UserSportProfile.create(
                lp = initLp,
                user = user,
                sport = sport,
                tier = tier,
            )
        )

        user.updateActiveProfile(profile.id!!)
    }

    private fun validateAlreadyRegisteredSport(userId: String, sportId: Long) {
        if (userSportProfileRepository.existsByUserIdAndSportId(userId, sportId)) {
            throw CustomException(ErrorCode.ALREADY_EXIST_SPORT_PROFILE)
        }
    }

    @Transactional(readOnly = true)
    fun getUserProfiles(
        userId: String,
    ): UserProfilesResponse {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val allProfiles = userSportProfileRepository.findAllByUserIdOrderBySportName(userId)

        val activeProfile = allProfiles.find { it.id == user.activeUserSportProfileId }
            ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)

        val reviews = gameReviewRepository.countByRevieweeAndSport(
            revieweeUserId = userId,
            sportId = activeProfile.sport.id!!,
        )

        return UserProfilesResponse.from(
            nickname = user.nickname,
            gender = user.gender,
            reviews = reviews,
            activeProfile = activeProfile,
            allProfiles = allProfiles,
        )
    }

    @Transactional(readOnly = true)
    fun getOtherUserProfiles(
        userId: String,
        otherUserId: String,
        sportCode: String?,
    ): OtherUserProfilesResponse {
        val myInfo = getMyInfoAndActiveProfile(userId)

        val otherUser = userRepository.findByIdOrNull(otherUserId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val allProfiles = userSportProfileRepository.findAllByUserIdOrderBySportName(otherUserId)

        val selectedProfile = if (sportCode == null) {
            allProfiles.find { myInfo.activeProfile.sport.code == it.sport.code }
                ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)
        } else {
            allProfiles.find { it.sport.code == sportCode }
                ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)
        }

        val reviews = gameReviewRepository.countByRevieweeAndSport(
            revieweeUserId = otherUserId,
            sportId = selectedProfile.sport.id!!,
        )

        // 매칭 신청 가능 여부 확인
        val isChallengeable = checkIsChallengeable(
            myProfileId = myInfo.activeProfile.id!!,
            otherProfileId = selectedProfile.id!!,
            sportId = selectedProfile.sport.id!!
        )

        // 매칭 수락 가능 여부 확인
        val receivedMatching = matchingRepository.findFirstByReceiverProfileIdAndRequesterProfileIdAndSportIdAndStatusOrderByCreatedAtDesc(
            receiverProfileId = myInfo.activeProfile.id!!,
            requesterProfileId = selectedProfile.id!!,
            sportId = selectedProfile.sport.id!!,
            status = MatchingStatus.REQUESTED
        )

        return OtherUserProfilesResponse.from(
            nickname = otherUser.nickname,
            gender = otherUser.gender,
            reviews = reviews,
            selectedProfile = selectedProfile,
            allProfiles = allProfiles,
            isChallengeable = isChallengeable,
            isAcceptable = receivedMatching != null,
            receivedMatchingId = receivedMatching?.id,
        )
    }

    private fun checkIsChallengeable(
        myProfileId: String,
        otherProfileId: String,
        sportId: Long,
    ): Boolean {
        // 하루 3판 제한 (RESULT_CONFIRMED 게임 기준)
        val isDailyLimitValid = validateDailyLimit(
            profileA = myProfileId,
            profileB = otherProfileId,
            sportId = sportId,
        )

        // 24h 쿨다운 (요청/취소/거절 기준)
        val isCooldownValid = runCatching {
            validateCooldown(
                profileA = myProfileId,
                profileB = otherProfileId,
                sportId = sportId,
                now = TimeUtils.nowOffsetDateTime().toLocalDateTime(),
            )
        }.isSuccess

        return isDailyLimitValid && isCooldownValid
    }

    private fun validateDailyLimit(
        profileA: String,
        profileB: String,
        sportId: Long,
    ): Boolean {
        val today = LocalDate.now(DEFAULT_ZONE_ID)
        val startOfDay = today.atStartOfDay()
        val endOfDay = today.plusDays(1).atStartOfDay()

        val confirmedCount = gameRepository.countConfirmedGamesTodayBetweenProfiles(
            profileA = profileA,
            profileB = profileB,
            sportId = sportId,
            startOfDay = startOfDay,
            endOfDay = endOfDay,
        )

        return confirmedCount < 3L
    }

    private fun validateCooldown(
        profileA: String,
        profileB: String,
        sportId: Long,
        now: LocalDateTime,
    ) {
        val latest = matchingRepository.findLatestForCooldown(
            profileA = profileA,
            profileB = profileB,
            sportId = sportId,
        ) ?: return

        val status = runCatching { MatchingStatus.valueOf(latest.status) }
            .getOrElse { return }

        when (status) {
            MatchingStatus.REQUESTED -> {
                val until = latest.createdAt.plusHours(24)
                if (now.isBefore(until)) throw CustomException(ErrorCode.MATCHING_PENDING_EXISTS)
            }

            MatchingStatus.CANCELLED,
            MatchingStatus.REJECTED -> {
                val base = latest.respondedAt ?: latest.createdAt
                val until = base.plusHours(24)
                if (now.isBefore(until)) throw CustomException(ErrorCode.MATCHING_PENDING_EXISTS)
            }

            else -> Unit
        }
    }

    @Transactional
    fun updateRegion(
        userId: String,
        requestCommand: AddressUpdateCommand,
    ) {
        val user = userRepository.findByIdOrNull(userId) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val trimmedRegion = requestCommand.region.trim()
        validateRegion(trimmedRegion)

        user.updateRegion(trimmedRegion)
    }

    private fun validateRegion(region: String) {
        if (!region.endsWith(DISTRICT_SUFFIX)) {
            throw CustomException(ErrorCode.INVALID_REGION)
        }
    }

    @Transactional
    fun updateActiveProfile(
        userId: String,
        requestCommand: ActiveProfileUpdateCommand,
    ) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        if (!userSportProfileRepository.existsById(requestCommand.profileId)) {
            throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)
        }

        user.updateActiveProfile(requestCommand.profileId)
    }

    @Transactional(readOnly = true)
    fun getOtherUsersRecommendation(
        userId: String,
    ): OtherUsersRecommendationResponse {
        val myInfo = getMyInfoAndActiveProfile(userId)

        val recommendedProfiles = userSportProfileRepository.findRandomRecommendation(
            region = myInfo.user.region,
            sportId = myInfo.activeProfile.sport.id!!,
            excludeUserId = myInfo.user.id!!,
            myLp = myInfo.activeProfile.lp,
            lpThreshold = LP_THRESHOLD,
            limit = LIMIT_RECOMMEND
        )

        return OtherUsersRecommendationResponse.from(recommendedProfiles)
    }

    @Transactional(readOnly = true)
    fun getOtherUsersLeaderBoard(
        userId: String,
    ): OtherUsersLeaderBoardResponse {
        val myInfo = getMyInfoAndActiveProfile(userId)

        val leaderBoardProfiles = userSportProfileRepository.findAllByRegionAndSportOrderByLp(
            region = myInfo.user.region,
            sportId = myInfo.activeProfile.sport.id!!,
        )

        return OtherUsersLeaderBoardResponse.from(
            topUsers = leaderBoardProfiles,
            nickname = myInfo.user.nickname,
            tierCode = myInfo.activeProfile.tier.code,
            lp = myInfo.activeProfile.lp,
        )
    }

    @Transactional(readOnly = true)
    fun getOtherUserSearch(
        userId: String,
        requestCommand: OtherUserSearchCommand,
    ): OtherUserSearchResponse {
        val myInfo = getMyInfoAndActiveProfile(userId)

        val otherUsersSearch = userSportProfileRepository.findAllBySportOrderByNickname(
            nickname = requestCommand.nickname,
            sportId = myInfo.activeProfile.sport.id!!,
            excludeUserId = userId,
        )

        return OtherUserSearchResponse.from(otherUsersSearch)
    }

    @Transactional(readOnly = true)
    fun getUserRecentReview(
        userId: String,
        request: CommonCursorRequest
    ): CursorResponse<UserRecentReviewResponse> {
        val myInfo = getMyInfoAndActiveProfile(userId)
        val snapshotAt = request.snapshotAt ?: OffsetDateTime.now()
        val sportId = myInfo.activeProfile.sport.id ?: throw CustomException(ErrorCode.SPORT_NOT_FOUND)

        val response = gameReviewRepository.findAllBySportIdOrderByDate(
            request = request,
            sportId = sportId,
            userId = userId,
            snapshotAt = snapshotAt,
        )

        return CursorResponse(
            snapshotAt = response.snapshotAt,
            results = UserRecentReviewResponse.listForm(response.results),
            nextCursor = response.nextCursor,
            hasNext = response.hasNext,
        )
    }

    @Transactional(readOnly = true)
    fun getUserRecentReviewSummary(
        userId: String,
    ): UserRecentReviewSummaryResponse {
        val myInfo = getMyInfoAndActiveProfile(userId)

        val sportId = myInfo.activeProfile.sport.id!!

        val counts = getCounts(
            userId = userId,
            sportId = sportId
        )

        return UserRecentReviewSummaryResponse.from(
            ratingMap = counts.ratingMap,
            tagMap = counts.tagMap,
        )
    }

    @Transactional(readOnly = true)
    fun getOtherUserRecentReview(
        userId: String,
        otherUserId: String,
        sportCode: String?,
        request: CommonCursorRequest,
    ): CursorResponse<UserRecentReviewResponse> {
        val otherUser = userRepository.findByIdOrNull(otherUserId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val selectedProfile = resolveProfile(
            userId = userId,
            otherUser = otherUser,
            sportCode = sportCode,
        )

        val sportId = selectedProfile.sport.id!!

        val snapshotAt = request.snapshotAt ?: OffsetDateTime.now()

        val response = gameReviewRepository.findAllBySportIdOrderByDate(
            request = request,
            sportId = sportId,
            userId = otherUserId,
            snapshotAt = snapshotAt
        )

        return CursorResponse(
            snapshotAt = snapshotAt,
            results = UserRecentReviewResponse.listForm(response.results),
            nextCursor = response.nextCursor,
            hasNext = response.hasNext
        )
    }

    @Transactional(readOnly = true)
    fun getOtherUserRecentReviewSummary(
        userId: String,
        otherUserId: String,
        sportCode: String?,
    ): UserRecentReviewSummaryResponse {
        val otherUser = userRepository.findByIdOrNull(otherUserId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val selectedProfile = resolveProfile(
            userId = userId,
            otherUser = otherUser,
            sportCode = sportCode,
        )

        val sportId = selectedProfile.sport.id!!

        val counts = getCounts(
            userId = otherUserId,
            sportId = sportId
        )

        return UserRecentReviewSummaryResponse.from(
            ratingMap = counts.ratingMap,
            tagMap = counts.tagMap,
        )
    }

    private fun resolveProfile(
        userId: String,
        otherUser: User,
        sportCode: String?
    ) = if (sportCode == null) {
        val myInfo = getMyInfoAndActiveProfile(userId)
        userSportProfileRepository.findByUserIdAndSportCode(
            userId = otherUser.id!!,
            sportCode = myInfo.activeProfile.sport.code,
        ) ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)
    } else {
        userSportProfileRepository.findByUserIdAndSportCode(
            userId = otherUser.id!!,
            sportCode = sportCode,
        ) ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)
    }

    private fun getCounts(
        userId: String,
        sportId: Long
    ): ReviewCountsResult {
        val ratingResults = gameReviewRepository.countRatingsByRevieweeAndSport(
            revieweeId = userId,
            sportId = sportId,
        )
        val ratingMap = ratingResults.associate { data ->
            data.reviewRating to data.counts
        }

        val tagResults = gameReviewRepository.countTagsByRevieweeAndSport(
            revieweeId = userId,
            sportId = sportId,
        )
        val tagMap = tagResults.associate { data ->
            data.reviewTag to data.counts
        }

        return ReviewCountsResult(
            ratingMap = ratingMap,
            tagMap = tagMap,
        )
    }

    @Transactional(readOnly = true)
    fun getOtherUserRegion(
        userId: String,
        requestCommand: OtherUserRegionCommand,
        requestCursor: CommonCursorRequest,
    ): CursorResponse<OtherUserRegionResponse> {
        val myInfo = getMyInfoAndActiveProfile(userId)
        val sportId = myInfo.activeProfile.sport.id!!

        val snapshotAt = requestCursor.snapshotAt ?: TimeUtils.nowOffsetDateTime()

        val response = userSportProfileRepository.findAllBySportAndRegion(
            userId = userId,
            sportId = sportId,
            region = myInfo.user.region,
            request = requestCursor,
            gender = requestCommand.gender,
            tier = requestCommand.tier?.name,
            snapshotAt = snapshotAt,
        )

        return CursorResponse(
            snapshotAt = response.snapshotAt,
            results = OtherUserRegionResponse.listForm(response.results),
            nextCursor = response.nextCursor,
            hasNext = response.hasNext,
        )
    }

    private fun getMyInfoAndActiveProfile(userId: String): UserWithActiveProfile {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val activeProfile = userSportProfileRepository.findByIdOrNull(user.activeUserSportProfileId!!)
            ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)

        return UserWithActiveProfile(
            user = user,
            activeProfile = activeProfile,
        )
    }

    @Transactional(readOnly = true)
    fun getUserRegion(
        userId: String,
    ): UserRegionResponse {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        return UserRegionResponse.from(
            region = user.region
        )
    }

    companion object {
        private val NICKNAME_VALID_REGEX = Regex("^[a-zA-Z0-9가-힣]*$")
        private const val MAX_NICKNAME_LENGTH = 10
        val OPEN_CHAT_URL_REGEX = Regex("^https://open\\.kakao\\.com/o/[a-zA-Z0-9]+\$")
        private const val LP_THRESHOLD = 200
        private const val LIMIT_RECOMMEND = 5L
        const val DISTRICT_SUFFIX = "구"
    }
}
