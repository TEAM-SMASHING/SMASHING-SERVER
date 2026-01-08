package org.appjam.smashing.domain.sport.enums

import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode

enum class TierType(
    val tierName: String,
    val orderNo: Int,
    val initTier: Int?,
    val minLp: Int,
    val maxLp: Int,
) {
    IRON(
        tierName = "아이언",
        orderNo = 1,
        initTier = 0,
        minLp = 0,
        maxLp = 99,
    ),

    BRONZE_3(
        tierName = "브론즈 3",
        orderNo = 2,
        initTier = 100,
        minLp = 100,
        maxLp = 199,
    ),
    BRONZE_2(
        tierName = "브론즈 2",
        orderNo = 3,
        initTier = 200,
        minLp = 200,
        maxLp = 299,
    ),
    BRONZE_1(
        tierName = "브론즈 1",
        orderNo = 4,
        initTier = 300,
        minLp = 300,
        maxLp = 399,
    ),

    SILVER_3(
        tierName = "실버 3",
        orderNo = 5,
        initTier = 400,
        minLp = 400,
        maxLp = 499,
    ),
    SILVER_2(
        tierName = "실버 2",
        orderNo = 6,
        initTier = null,
        minLp = 500,
        maxLp = 599,
    ),
    SILVER_1(
        tierName = "실버 1",
        orderNo = 7,
        initTier = null,
        minLp = 600,
        maxLp = 699,
    ),

    GOLD_3(
        tierName = "골드 3",
        orderNo = 8,
        initTier = null,
        minLp = 700,
        maxLp = 799,
    ),
    GOLD_2(
        tierName = "골드 2",
        orderNo = 9,
        initTier = null,
        minLp = 800,
        maxLp = 899,
    ),
    GOLD_1(
        tierName = "골드 1",
        orderNo = 10,
        initTier = null,
        minLp = 900,
        maxLp = 999,
    ),

    PLATINUM_3(
        tierName = "플래티넘 3",
        orderNo = 11,
        initTier = null,
        minLp = 1000,
        maxLp = 1099,
    ),
    PLATINUM_2(
        tierName = "플래티넘 2",
        orderNo = 12,
        initTier = null,
        minLp = 1100,
        maxLp = 1199,
    ),
    PLATINUM_1(
        tierName = "플래티넘 1",
        orderNo = 13,
        initTier = null,
        minLp = 1200,
        maxLp = 1299,
    ),

    DIAMOND_3(
        tierName = "다이아 3",
        orderNo = 14,
        initTier = null,
        minLp = 1300,
        maxLp = 1399,
    ),
    DIAMOND_2(
        tierName = "다이아 2",
        orderNo = 15,
        initTier = null,
        minLp = 1400,
        maxLp = 1499,
    ),
    DIAMOND_1(
        tierName = "다이아 1",
        orderNo = 16,
        initTier = null,
        minLp = 1500,
        maxLp = 1599,
    ),

    CHALLENGER(
        tierName = "챌린저",
        orderNo = 17,
        initTier = null,
        minLp = 1600,
        maxLp = Int.MAX_VALUE,
    ),
    ;

    companion object {
        fun from(name: String): TierType =
            entries.find { it.name == name.uppercase() }
                ?: throw CustomException(ErrorCode.INVALID_TIER_NAME)
    }
}
