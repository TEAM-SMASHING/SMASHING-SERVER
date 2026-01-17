package org.appjam.smashing.domain.sport.enums

enum class ExperienceRange(
    val initLp: Int,
) {
    LT_3_MONTHS(0),
    LT_6_MONTHS(100),
    LT_1_YEAR(200),
    LT_1_6_YEARS(300),
    GTE_2_YEARS(400),
    ;
}
