package org.appjam.smashing.domain.user.dto.command

import org.appjam.smashing.domain.tier.enums.TierGroup
import org.appjam.smashing.domain.user.enums.Gender

data class OtherUserRegionCommand(
    val gender: Gender?,
    val tier: TierGroup?
)
