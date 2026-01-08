package org.appjam.smashing.domain.auth.social.kakao.dto.response

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class KakaoUserResponse(
    val id: Long,
    val kakaoAccount: KakaoAccount?
) {
    data class KakaoAccount(
        val profile: Profile?,
        val email: String?
    )

    data class Profile(
        val nickname: String?,
        val profileImageUrl: String?
    )
}
