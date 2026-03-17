package org.appjam.smashing.domain.block.service

import org.appjam.smashing.domain.block.dto.command.UserBlockCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BlockService {
    @Transactional
    fun blockUser(
        userId: String,
        requestCommand: UserBlockCommand,
    ) {

    }
}
