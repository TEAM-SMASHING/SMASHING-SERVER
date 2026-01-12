package org.appjam.smashing.domain.user.dto.request

import org.appjam.smashing.domain.user.dto.command.ActiveProfileUpdateCommand

data class ActiveProfileUpdateRequest(
    val profileId: String,
) {
    fun toCommand() = ActiveProfileUpdateCommand(
        profileId = profileId,
    )
}
