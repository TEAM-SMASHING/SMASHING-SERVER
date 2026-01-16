package org.appjam.smashing.domain.user.service

import org.appjam.smashing.domain.review.enums.ReviewRating
import org.appjam.smashing.domain.review.enums.ReviewTag
import org.appjam.smashing.domain.review.repository.GameReviewRepository
import org.appjam.smashing.domain.sport.enums.InitTierLp
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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userSportProfileRepository: UserSportProfileRepository,
    private val sportRepository: SportRepository,
    private val tierRepository: TierRepository,
    private val gameReviewRepository: GameReviewRepository,
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

        val tierName = requestCommand.tier
        val initTier = runCatching { InitTierLp.valueOf(tierName) }.getOrNull()
            ?: throw CustomException(ErrorCode.INVALID_INITIAL_TIER)
        val tier = tierRepository.findBySportIdAndName(
            sportId = sport.id!!,
            name = tierName,
        ) ?: throw CustomException(ErrorCode.INVALID_TIER_SETTING)

        val profile = userSportProfileRepository.save(
            UserSportProfile.create(
                lp = initTier.initLp,
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

        return UserProfilesResponse.from(
            nickname = user.nickname,
            activeProfile = activeProfile,
            allProfiles = allProfiles,
        )
    }

    @Transactional(readOnly = true)
    fun getOtherUserProfiles(
        otherUserId: String,
        sportCode: String?,
    ): OtherUserProfilesResponse {
        val otherUser = userRepository.findByIdOrNull(otherUserId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val allProfiles = userSportProfileRepository.findAllByUserIdOrderBySportName(otherUserId)

        val selectedProfile = if (sportCode == null) {
            allProfiles.find { it.id == otherUser.activeUserSportProfileId }
                ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)
        } else {
            allProfiles.find { it.sport.code == sportCode }
                ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)
        }

        return OtherUserProfilesResponse.from(
            nickname = otherUser.nickname,
            selectedProfile = selectedProfile,
            allProfiles = allProfiles
        )
    }

    @Transactional
    fun updateRegion(
        userId: String,
        requestCommand: AddressUpdateCommand,
    ) {
        val user = userRepository.findByIdOrNull(userId) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        // TODO: 지역 관련 검증 로직 추가 필요

        user.updateRegion(requestCommand.region)
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
        val (user, activeProfile) = getMyInfoAndActiveProfile(userId)

        val recommendedProfiles = userSportProfileRepository.findRandomRecommendation(
            region = user.region,
            sportId = activeProfile.sport.id!!,
            excludeUserId = user.id!!,
            myLp = activeProfile.lp,
            lpThreshold = LP_THRESHOLD,
            limit = LIMIT_RECOMMEND
        )

        return OtherUsersRecommendationResponse.from(recommendedProfiles)
    }

    @Transactional(readOnly = true)
    fun getOtherUsersLeaderBoard(
        userId: String,
    ): OtherUsersLeaderBoardResponse {
        val (user, activeProfile) = getMyInfoAndActiveProfile(userId)

        val leaderBoardProfiles = userSportProfileRepository.findAllByRegionAndSportOrderByLp(
            region = user.region,
            sportId = activeProfile.sport.id!!,
            excludeUserId = user.id!!
        )

        return OtherUsersLeaderBoardResponse.from(
            topUsers = leaderBoardProfiles
        )
    }

    @Transactional(readOnly = true)
    fun getOtherUserSearch(
        userId: String,
        requestCommand: OtherUserSearchCommand,
    ): OtherUserSearchResponse {
        val (user, activeProfile) = getMyInfoAndActiveProfile(userId)

        val otherUsersSearch = userSportProfileRepository.findAllBySportOrderByNickname(
            nickname = requestCommand.nickname,
            sportId = activeProfile.sport.id!!,
            excludeUserId = userId,
        )

        return OtherUserSearchResponse.from(otherUsersSearch)
    }

    @Transactional(readOnly = true)
    fun getUserRecentGame(
        userId: String,
        request: CommonCursorRequest
    ): CursorResponse<UserRecentGameResult, UserRecentGameMeta> {
        val (_, activeProfile) = getMyInfoAndActiveProfile(userId)
        val snapshotAt = request.snapshotAt ?: OffsetDateTime.now()
        val sportId = activeProfile.sport.id ?: throw CustomException(ErrorCode.SPORT_NOT_FOUND)

        val page = gameReviewRepository.findAllBySportIdOrderByDate(
            request = request,
            sportId = sportId,
            userId = userId,
            snapshotAt = snapshotAt,
        )

        val (ratingCounts, tagCounts) = getCounts(
            userId = userId,
            sportId = sportId,
        )
        val countsMeta = UserRecentGameMeta(
            ratingCounts = ratingCounts,
            tagCounts = tagCounts
        )

        return CursorResponse.from(
            page = page,
            meta = countsMeta,
            results = UserRecentGameResult.listForm(page.results),
        )
    }

    @Transactional(readOnly = true)
    fun getOtherUserRecentGame(
        userId: String,
        otherUserId: String,
        sportCode: String?,
        request: CommonCursorRequest,
    ): CursorResponse<UserRecentGameResult, UserRecentGameMeta> {
        val otherUser = userRepository.findByIdOrNull(otherUserId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val selectedProfile = resolveProfile(
            user = otherUser,
            sportCode = sportCode,
        )

        val sportId = selectedProfile.sport.id!!

        val snapshotAt = request.snapshotAt ?: OffsetDateTime.now()

        val page = gameReviewRepository.findAllBySportIdOrderByDate(
            request = request,
            sportId = sportId,
            userId = otherUserId,
            snapshotAt = snapshotAt
        )

        val (ratingCounts, tagCounts) = getCounts(
            userId = otherUserId,
            sportId = sportId,
        )
        val countsMeta = UserRecentGameMeta(
            ratingCounts = ratingCounts,
            tagCounts = tagCounts
        )

        return CursorResponse.from(
            page = page,
            meta = countsMeta,
            results = UserRecentGameResult.listForm(page.results),
        )
    }

    private fun resolveProfile(user: User, sportCode: String?): UserSportProfile =
        if (sportCode == null) {
            val activeProfileId = user.activeUserSportProfileId
                ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)
            userSportProfileRepository.findByIdOrNull(activeProfileId)
                ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)
        } else {
            userSportProfileRepository.findByUserIdAndSportCode(user.id!!, sportCode)
                ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)
        }

    private fun getCounts(
        userId: String,
        sportId: Long,
    ): CountsResult {
        val ratingResults = gameReviewRepository.countRatingsByRevieweeAndSport(
            revieweeId = userId,
            activeSportId = sportId,
        )
        val ratingMap = ratingResults.associate { data ->
            data.reviewRating to data.counts?.toInt()
        }
        val ratingCounts = UserRecentGameMeta.RatingCounts.from(
            best = ratingMap[ReviewRating.BEST] ?: 0,
            good = ratingMap[ReviewRating.GOOD] ?: 0,
            bad = ratingMap[ReviewRating.BAD] ?: 0
        )

        val tagResults = gameReviewRepository.countTagsByRevieweeAndSport(
            revieweeId = userId,
            activeSportId = sportId,
        )
        val tagMap = tagResults.associate { data ->
            data.reviewTag to data.counts?.toInt()
        }
        val tagCounts = UserRecentGameMeta.TagCounts.from(
            goodManner = tagMap[ReviewTag.GOOD_MANNER] ?: 0,
            onTime = tagMap[ReviewTag.ON_TIME] ?: 0,
            fairPlay = tagMap[ReviewTag.FAIR_PLAY] ?: 0,
            fastResponse = tagMap[ReviewTag.FAST_RESPONSE] ?: 0
        )

        return CountsResult(
            ratingCounts = ratingCounts,
            tagCounts = tagCounts,
        )
    }

    @Transactional(readOnly = true)
    fun getOtherUserRegion(
        userId: String,
        requestCommand: OtherUserRegionCommand,
        requestCursor: CommonCursorRequest,
    ): CursorResponse<OtherUserRegionResponse, Unit> {
        val (user, activeProfile) = getMyInfoAndActiveProfile(userId)
        val sportId = activeProfile.sport.id ?: throw CustomException(ErrorCode.SPORT_NOT_FOUND)

        val snapshotAt = requestCursor.snapshotAt ?: TimeUtils.nowOffsetDateTime()

        val response = userSportProfileRepository.findAllBySportAndRegion(
            userId = userId,
            sportId = sportId,
            region = user.region,
            request = requestCursor,
            gender = requestCommand.gender?.name,
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

    private fun getMyInfoAndActiveProfile(userId: String): Pair<User, UserSportProfile> {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val activeProfile = userSportProfileRepository.findByIdOrNull(user.activeUserSportProfileId!!)
            ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)

        return user to activeProfile
    }

    companion object {
        private val NICKNAME_VALID_REGEX = Regex("^[a-zA-Z0-9가-힣]*$")
        private const val MAX_NICKNAME_LENGTH = 10
        val OPEN_CHAT_URL_REGEX = Regex("^https://open\\.kakao\\.com/o/[a-zA-Z0-9]+\$")
        private const val LP_THRESHOLD = 200
        private const val LIMIT_RECOMMEND = 5L
    }
}

data class CountsResult(
    val ratingCounts: UserRecentGameMeta.RatingCounts,
    val tagCounts: UserRecentGameMeta.TagCounts
)
