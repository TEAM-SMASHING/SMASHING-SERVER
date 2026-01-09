package org.appjam.smashing.domain.user.service

import org.appjam.smashing.domain.user.dto.response.NicknameCheckResponse
import org.appjam.smashing.domain.user.repository.UserRepository
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


        return if (userRepository.existsByNickname(nickname)) {
            NicknameCheckResponse(false)
        } else {
            NicknameCheckResponse(true)
        }
    }
}
