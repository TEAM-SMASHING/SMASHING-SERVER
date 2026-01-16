package org.appjam.smashing.global.util

import com.querydsl.core.types.dsl.ComparableExpression
import org.appjam.smashing.global.common.enums.OrderType

object CursorQueryUtils {

    fun resolveOrderType(
        str: String?
    ) = str?.trim()
            ?.uppercase()
            ?.let { runCatching { OrderType.valueOf(it) }.getOrNull() }
            ?: OrderType.LATEST

    fun <T : Comparable<T>> orderBy(
        path: ComparableExpression<T>,
        orderType: OrderType,
    ) = when (orderType) {
            OrderType.OLDEST -> path.asc()
            else -> path.desc()
        }

    fun <T : Comparable<T>> cursorWhere(
        path: ComparableExpression<T>,
        orderType: OrderType,
        cursorValue: T?,
    ) = cursorValue?.let {
            when (orderType) {
                OrderType.OLDEST -> path.gt(it)
                else -> path.lt(it)
            }
        }
}
