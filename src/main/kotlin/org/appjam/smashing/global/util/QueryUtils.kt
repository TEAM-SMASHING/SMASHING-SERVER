package org.appjam.smashing.global.util

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberTemplate

object QueryUtils {
    val randomOrder: NumberTemplate<Double> = Expressions.numberTemplate(Double::class.java, "function('RAND')")
}