package org.appjam.smashing.global.util

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

object TimeUtils {
    /*
     * 기본 타임존 상수
     */
    val DEFAULT_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")

    /*
     * LocalDateTime -> OffsetDateTime 변환
     */
    fun toOffsetDateTime(
        localDateTime: LocalDateTime
    )= localDateTime.atZone(DEFAULT_ZONE_ID).toOffsetDateTime()

    /*
     * 현재 LocalDateTime 조회
     */
    fun nowOffsetDateTime() = OffsetDateTime.now(DEFAULT_ZONE_ID)
}
