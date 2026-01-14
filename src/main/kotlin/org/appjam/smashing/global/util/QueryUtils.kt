package org.appjam.smashing.global.util

import com.querydsl.core.types.dsl.Expressions

object QueryUtils {
    val randomOrder = Expressions.numberTemplate(Double::class.java, "function('RAND')")
}