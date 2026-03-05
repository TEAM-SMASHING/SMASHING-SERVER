package org.appjam.smashing.domain.matching.dto.projection

import java.time.LocalDateTime

interface LatestMatchingCooldownProjection {
    val status: String
    val createdAt: LocalDateTime
    val respondedAt: LocalDateTime?
}
