package org.appjam.smashing.domain.user.service

import org.appjam.smashing.domain.user.dto.command.OpenChatValidateCommand
import org.appjam.smashing.domain.user.dto.response.NicknameCheckResponse
import org.appjam.smashing.domain.user.dto.response.OpenChatValidateResponse
import org.appjam.smashing.domain.user.repository.UserRepository
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
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

    companion object {
        private val NICKNAME_VALID_REGEX = Regex("^[a-zA-Z0-9가-힣]*$")
        private const val MAX_NICKNAME_LENGTH = 10
        val OPEN_CHAT_URL_REGEX = Regex("^https://open\\.kakao\\.com/o/[a-zA-Z0-9]+\$")
    }
}
