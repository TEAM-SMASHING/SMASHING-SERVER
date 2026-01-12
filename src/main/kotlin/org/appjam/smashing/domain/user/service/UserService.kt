package org.appjam.smashing.domain.user.service

import org.appjam.smashing.domain.sport.enums.InitTierLp
import org.appjam.smashing.domain.sport.repository.SportRepository
import org.appjam.smashing.domain.tier.repository.TierRepository
import org.appjam.smashing.domain.user.dto.command.AddressUpdateCommand
import org.appjam.smashing.domain.user.dto.command.OpenChatValidateCommand
import org.appjam.smashing.domain.user.dto.command.ProfileAddCommand
import org.appjam.smashing.domain.user.dto.response.NicknameCheckResponse
import org.appjam.smashing.domain.user.dto.response.OpenChatValidateResponse
import org.appjam.smashing.domain.user.dto.response.OtherUserProfilesResponse
import org.appjam.smashing.domain.user.dto.response.UserProfileTierResponse
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

        val allProfiles = userSportProfileRepository.findAllByUserId(userId)

        val activeProfile = allProfiles.find { it.id == user.activeUserSportProfileId }
            ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)

        val sportsList = allProfiles
            .filter { it.id != user.activeUserSportProfileId }
            .map {
                UserProfileTierResponse.SportInfo.from(
                    profileId = it.id!!,
                    sportCode = it.sport.code
                )
            }

        return UserProfileTierResponse(
            activeSport = UserProfileTierResponse.ActiveSport.from(
                profileId = activeProfile.id!!,
                sportCode = activeProfile.sport.code,
                tierId = activeProfile.tier.orderNo,
                lp = activeProfile.lp,
                minLp = activeProfile.tier.minLp,
                maxLp = activeProfile.tier.maxLp,
                wins = activeProfile.wins,
                losses = activeProfile.losses
            ),
            sports = sportsList
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
    fun getOtherUserProfiles(
        otherUserId: String,
        sportCode: String?,
    ): OtherUserProfilesResponse {
        val otherUser = userRepository.findByIdOrNull(otherUserId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val allProfiles = userSportProfileRepository.findAllByUserId(otherUserId)

        val selectedSport = if (sportCode == null) {
            allProfiles.find { it.id == otherUser.activeUserSportProfileId }
                ?: throw CustomException(ErrorCode.ACTIVE_PROFILE_NOT_FOUND)
        } else {
            allProfiles.find { it.sport.code == sportCode }
                ?: throw CustomException(ErrorCode.USER_SPORT_PROFILE_NOT_FOUND)
        }

        return OtherUserProfilesResponse.from(
            nickname = otherUser.nickname,
            selectedSport = selectedSport,
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

    companion object {
        private val NICKNAME_VALID_REGEX = Regex("^[a-zA-Z0-9가-힣]*$")
        private const val MAX_NICKNAME_LENGTH = 10
        val OPEN_CHAT_URL_REGEX = Regex("^https://open\\.kakao\\.com/o/[a-zA-Z0-9]+\$")
    }
}
