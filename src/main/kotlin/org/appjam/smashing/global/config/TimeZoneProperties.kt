package org.appjam.smashing.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "timezone")
data class TimeZoneProperties(
    val defaultTimeZone: String,
)
