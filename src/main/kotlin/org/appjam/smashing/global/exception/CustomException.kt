package org.appjam.smashing.global.exception

class CustomException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.errorCode) {
}
