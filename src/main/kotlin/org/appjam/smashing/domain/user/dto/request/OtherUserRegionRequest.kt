package org.appjam.smashing.domain.user.dto.request

import org.appjam.smashing.domain.tier.enums.TierGroup
import org.appjam.smashing.domain.user.dto.command.OtherUserRegionCommand
import org.appjam.smashing.domain.user.enums.Gender
import org.appjam.smashing.global.extensions.ofIgnoreCase

data class OtherUserRegionRequest(
    val tier: String?,
    val gender: String?
) {
    fun toCommand() = OtherUserRegionCommand(
        tier = tier?.takeIf { it.isNotBlank() }?.let { ofIgnoreCase<TierGroup>(it) },
        gender = gender?.takeIf { it.isNotBlank() }?.let { ofIgnoreCase<Gender>(it) }
    )
}
