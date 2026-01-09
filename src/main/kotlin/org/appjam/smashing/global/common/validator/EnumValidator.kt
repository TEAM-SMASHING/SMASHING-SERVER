package org.appjam.smashing.global.common.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.appjam.smashing.global.common.validator.annotation.ValidEnum
import kotlin.reflect.KClass

class EnumValidator : ConstraintValidator<ValidEnum, String?> {

    private lateinit var enumClass: KClass<out Enum<*>>

    override fun initialize(constraintAnnotation: ValidEnum) {
        enumClass = constraintAnnotation.enumClass
    }

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) return true

        return enumClass.java.enumConstants.any { it.name.equals(value, ignoreCase = true) }
    }
}
