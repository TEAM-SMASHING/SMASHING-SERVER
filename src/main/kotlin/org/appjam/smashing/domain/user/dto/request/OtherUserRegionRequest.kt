package org.appjam.smashing.domain.user.dto.request

import org.appjam.smashing.domain.tier.enums.TierGroup
import org.appjam.smashing.domain.user.dto.command.OtherUserRegionCommand
import org.appjam.smashing.domain.user.enums.Gender
import org.appjam.smashing.global.common.validator.annotation.ValidEnum
import org.appjam.smashing.global.extensions.ofIgnoreCaseOrNull

data class OtherUserRegionRequest(
    @field:ValidEnum(message = "잘못된 TierGroup 값입니다.", enumClass = TierGroup::class)
    val tier: String?,
    @field:ValidEnum(message = "잘못된 gender 값입니다.", enumClass = Gender::class)
    val gender: String?
) {
    fun toCommand() = OtherUserRegionCommand(
        tier = ofIgnoreCaseOrNull<TierGroup>(tier),
        gender = ofIgnoreCaseOrNull<Gender>(gender),
    )
}
