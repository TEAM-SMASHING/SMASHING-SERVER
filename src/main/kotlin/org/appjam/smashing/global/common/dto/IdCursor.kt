package org.appjam.smashing.global.common.dto

data class IdCursor(
    val id: String,
)

interface CursorKey {
    val cursorId: String
}
