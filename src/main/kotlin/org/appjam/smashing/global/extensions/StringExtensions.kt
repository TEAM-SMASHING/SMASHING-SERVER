package org.appjam.smashing.global.extensions

fun String.getActualLength(): Int = this.codePointCount(0, this.length)
