package org.appjam.smashing.global.util

import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.security.core.context.SecurityContextHolder
import java.time.ZoneId

object SecurityUtils {

    /**
     * 현재 SecurityContext에 저장된 인증 사용자 정보 조회
     * @return 현재 인증된 사용자(CustomUserDetails)
     */
    fun currentUser(): CustomUserDetails = (SecurityContextHolder.getContext().authentication?.principal as? CustomUserDetails)
            ?: throw CustomException(ErrorCode.UNAUTHORIZED)

    /**
     * 현재 인증된 사용자의 사용자 ID 조회
     * @return 현재 인증된 사용자의 ID
     */
    fun currentUserId(): String = currentUser().username

    /**
     * 현재 인증된 사용자의 타임존 정보 조회
     * @return 현재 인증된 사용자의 ZoneId
     */
    fun currentZoneId(): ZoneId = ZoneId.of(currentUser().getTimeZone())
}
