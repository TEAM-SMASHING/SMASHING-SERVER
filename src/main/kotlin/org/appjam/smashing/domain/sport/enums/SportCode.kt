package org.appjam.smashing.domain.sport.enums

import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode

enum class SportCode(
    val sportName: String,
) {
    TT(sportName = "탁구"),
    TN(sportName = "테니스"),
    BM(sportName = "배드민턴"),
    ;

    companion object {
        fun from(code: String): SportCode =
            entries.find { it.name == code.uppercase() }
                ?: throw CustomException(ErrorCode.INVALID_SPORT_CODE)
    }
}
