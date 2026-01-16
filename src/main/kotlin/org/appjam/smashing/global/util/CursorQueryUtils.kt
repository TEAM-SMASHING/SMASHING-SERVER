package org.appjam.smashing.global.util

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.ComparableExpression
import org.appjam.smashing.global.common.enums.OrderType

object CursorQueryUtils {

    fun resolveOrderType(raw: String?): OrderType =
        raw
            ?.trim()
            ?.uppercase()
            ?.let { runCatching { OrderType.valueOf(it) }.getOrNull() }
            ?: OrderType.LATEST

    fun <T : Comparable<T>> orderBy(
        path: ComparableExpression<T>,
        orderType: OrderType,
    ): OrderSpecifier<T> =
        when (orderType) {
            OrderType.OLDEST -> path.asc()
            else -> path.desc()
        }

    fun <T : Comparable<T>> cursorWhere(
        path: ComparableExpression<T>,
        orderType: OrderType,
        cursorValue: T?,
    ): BooleanExpression? =
        cursorValue?.let {
            when (orderType) {
                OrderType.OLDEST -> path.gt(it)
                else -> path.lt(it)
            }
        }
}
