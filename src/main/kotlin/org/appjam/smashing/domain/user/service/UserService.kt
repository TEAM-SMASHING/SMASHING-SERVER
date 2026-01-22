package org.appjam.smashing.domain.user.service

import org.appjam.smashing.domain.game.repository.GameRepository
import org.appjam.smashing.domain.matching.enums.MatchingStatus
import org.appjam.smashing.domain.matching.repository.MatchingRepository
import org.appjam.smashing.domain.matching.service.MatchingService
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

        // 하루 (00:00 ~) 최대 3회 게임 가능
        val validateDailyLimit = validateDailyLimit(
            requesterUserId = userId,
            receiverUserId = selectedProfile.user.id!!,
        )
        // 24시간 내 매칭 요청이 남아있는 경우, 동일 상대방에 대해 중복 매칭 요청 불가
        val validateNoMatchingRequest = validateNoMatchingRequestWithin24h(
            requesterUserId = userId,
            receiverUserId = selectedProfile.user.id!!,
        )

        val receivedMatching = matchingRepository.findFirstByReceiverIdAndRequesterIdAndSportIdAndStatusOrderByCreatedAtDesc(
            receiverId = userId,
            requesterId = otherUserId,
            sportId = selectedProfile.sport.id!!,
            status = MatchingStatus.REQUESTED
        )

        return OtherUserProfilesResponse.from(
            nickname = otherUser.nickname,
            gender = otherUser.gender,
            reviews = reviews,
            selectedProfile = selectedProfile,
            allProfiles = allProfiles,
            isChallengeable = validateDailyLimit && validateNoMatchingRequest,
            isAcceptable = receivedMatching != null,
            receivedMatchingId = receivedMatching?.id,
        )
    }

    private fun validateDailyLimit(
        requesterUserId: String,
        receiverUserId: String
    ): Boolean {
        val now = LocalDateTime.now(DEFAULT_ZONE_ID)
        val startOfDay = now.toLocalDate().atStartOfDay()

        // 하루 확정 게임 갯수 조회
        val todayConfirmedGames = gameRepository.countTodayConfirmedGamesBetweenUsers(
            startAt = startOfDay,
            userA = requesterUserId,
            userB = receiverUserId,
        )

        // 하루 최대 3회 제한
        return todayConfirmedGames < 3L
    }

    private fun validateNoMatchingRequestWithin24h(
        requesterUserId: String,
        receiverUserId: String,
    ): Boolean {
        val now = LocalDateTime.now(MatchingService.DEFAULT_ZONE_ID)
        val since = now.minusHours(24)

        val existsBlockedHistory = matchingRepository.existsBetweenUsersSinceExcludingAcceptedAndCompletedRaw(since, requesterUserId, receiverUserId) == 1L

        return !existsBlockedHistory
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
            userId = userId,
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
