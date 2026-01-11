package org.appjam.smashing.domain.user.command

data class ProfileAddCommand(
    val sportCode: String,
    val tier: String,
)
