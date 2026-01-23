package org.appjam.smashing.domain.user.dto.projection

import org.appjam.smashing.global.common.dto.CursorPayload

data class OtherUserRegionCursor(
    val reviewCount: Long,
    val totalGames: Long,
    val nickname: String,
) : CursorPayload
