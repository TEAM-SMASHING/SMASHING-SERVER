package org.appjam.smashing.global.common.dto

interface CursorPayload

interface CursorKey {
    val cursorId: String

    fun toCursorPayload(): CursorPayload = IdCursor(cursorId)
}

data class IdCursor(
    val id: String,
) : CursorPayload
