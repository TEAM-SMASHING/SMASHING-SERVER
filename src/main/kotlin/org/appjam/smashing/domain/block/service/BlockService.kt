package org.appjam.smashing.domain.block.service

import org.appjam.smashing.domain.block.dto.command.UserBlockCommand
import org.appjam.smashing.domain.block.entity.Block
import org.appjam.smashing.domain.block.repository.BlockRepository
import org.appjam.smashing.domain.user.repository.UserRepository
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlockService(
    private val userRepository: UserRepository,
    private val blockRepository: BlockRepository
) {
    @Transactional
    fun blockUser(
        userId: String,
        requestCommand: UserBlockCommand,
    ) {
        val blocker = userRepository.findByIdOrNull(userId)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val blockedUser = userRepository.findByIdOrNull(requestCommand.blockedUser)
            ?: throw CustomException(ErrorCode.BLOCKED_NOT_FOUND)

        // 조치1 - 자기 자신 차단 방지
        if (userId == blockedUser.id) {
            throw CustomException(ErrorCode.BLOCKED_SELF_FORBIDDEN)
        }

        // 조치2 - 중복 차단 불가(1)
        if (blockRepository.existsByBlockerAndBlockedUser(blocker, blockedUser)) {
            throw CustomException(ErrorCode.BLOCK_ALREADY_EXISTS)
        }

        // 조치2 - 중복 차단 불가(2)
        if (blockRepository.existsByBlockerAndBlockedUser(blockedUser, blocker)) {
            throw CustomException(ErrorCode.BLOCKED_BY_TARGET)
        }

        // 정책1 - 차단 데이터 저장
        val block = Block.create(
            blocker = blocker,
            blockedUser = blockedUser,
        )
        blockRepository.save(block)
    }
}
