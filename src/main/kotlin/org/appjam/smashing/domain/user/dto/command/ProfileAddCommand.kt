package org.appjam.smashing.domain.user.dto.command

import org.appjam.smashing.domain.sport.enums.ExperienceRange

data class ProfileAddCommand(
    val sportCode: String,
    val experienceRange: ExperienceRange,
)
