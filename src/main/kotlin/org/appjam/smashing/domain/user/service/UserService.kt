package org.appjam.smashing.domain.user.service

import org.appjam.smashing.domain.review.repository.GameReviewRepository
import org.appjam.smashing.domain.sport.enums.InitTierLp
import org.appjam.smashing.domain.sport.repository.SportRepository
import org.appjam.smashing.domain.tier.repository.TierRepository
import org.appjam.smashing.domain.user.dto.command.ActiveProfileUpdateCommand
import org.appjam.smashing.domain.user.dto.command.AddressUpdateCommand
import org.appjam.smashing.domain.user.dto.command.OpenChatValidateCommand
import org.appjam.smashing.domain.user.dto.command.ProfileAddCommand
import org.appjam.smashing.domain.user.dto.response.*
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.domain.user.repository.UserRepository
import org.appjam.smashing.domain.user.repository.UserSportProfileRepository
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

        val allProfiles = userSportProfileRepository.findAllByUserIdOrderByName(userId)

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

        val allProfiles = userSportProfileRepository.findAllByUserIdOrderByName(userId)

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

        val allProfiles = userSportProfileRepository.findAllByUserIdOrderByName(otherUserId)

        val selectedSport = if (sportCode == null) {
            allProfiles.find { it.id == otherUser.activeUserSportProfileId }
                ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)
        } else {
            allProfiles.find { it.sport.code == sportCode }
                ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)
        }

        return OtherUserProfilesResponse.from(
            nickname = otherUser.nickname,
            selectedProfile = selectedSport,
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
        // 유저 확인
        val user = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        // 유저 아이디의 모든 스포츠 프로필을 가져옴
        val allProfiles = userSportProfileRepository.findAllByUserId(userId)

        // 거기서 유저의 활성화 프로필을 가져옴
        val activeProfile = allProfiles.find { it.id == user.activeUserSportProfileId }
            ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)

        // 유저의 활성 지역 & 스포츠 아이디를 기준으로 추천 프로필 색출
        val allRecommendProfiles = userSportProfileRepository.findAllByRegionAndSport(
            region = user.region,
            sportId = activeProfile.sport.id!!,
            excludeUserId = user.id!!
        )

        // +-200 이내의 유저들 필터
        val filteredProfiles = allRecommendProfiles.filter { profile ->
            Math.abs(activeProfile.lp - profile.lp) <= MAX_SHUFFLE_LP
        }

        // 5명 정도 랜덤 뽑기
        val top5CycledProfiles = filteredProfiles
            .shuffled()
            .take(MAX_LP_GAP)

        // 랜덤 뽑은 유저의 아이디를 리스트로 뽑음
        val recommendedUserIds = top5CycledProfiles.map { profile ->
            profile.user.id!!
        }

        // 그 아이디를 기준으로 같은 스포츠의 리뷰를 List형태로 가져옴
        val reviewData = gameReviewRepository.countReviewsBySportAndReviewees(
            sportId = activeProfile.sport.id!!,
            userIds = recommendedUserIds
        )

        // 0번째를 String으로 1번째를 Long으로 형 변환
        val reviewMap = reviewData.associate { data ->
            data.recommendedUserId to data.reviewCount
        }

        // 그리고 이를 dto에 넣음
        return OtherUsersRecommendationResponse.from(
            recommendedUsers = top5CycledProfiles,
            reviewCounts = reviewMap
        )
    }

    companion object {
        private val NICKNAME_VALID_REGEX = Regex("^[a-zA-Z0-9가-힣]*$")
        private const val MAX_NICKNAME_LENGTH = 10
        val OPEN_CHAT_URL_REGEX = Regex("^https://open\\.kakao\\.com/o/[a-zA-Z0-9]+\$")
        private const val MAX_SHUFFLE_LP = 200
        private const val MAX_LP_GAP = 5
    }
}
