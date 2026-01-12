package org.appjam.smashing.global.common.dto

import jakarta.validation.constraints.Positive
import java.time.OffsetDateTime

data class CommonCursorRequest(
    @field:Positive(message = "페이지 사이즈는 양수입니다.")
    val size: Long = DEFAULT_SIZE,
    val order: String? = null,
    val cursor: String? = null,
    val snapshotAt: OffsetDateTime? = null,
) {
    companion object {
        private const val DEFAULT_SIZE: Long = 20
    }
}
