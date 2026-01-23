package org.appjam.smashing.global.common.dto

data class IdCursor(
    val id: String,
)

interface CursorKey {
    val cursorId: String
}

interface CompositeCursorKey : CursorKey {
    fun toCursorPayload(): Any
}
