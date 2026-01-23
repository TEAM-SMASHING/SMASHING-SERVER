package org.appjam.smashing.global.common.dto

import org.appjam.smashing.global.util.CursorCodec
import java.time.OffsetDateTime

interface CompositeCursorKey : CursorKey {
    fun toCursorPayload(): Any
}

data class CursorResponse<T>(
    val snapshotAt: OffsetDateTime,
    val results: List<T>,
    val nextCursor: String?,
    val hasNext: Boolean,
)

data class CursorPageResponse<T : CursorKey>(
    val snapshotAt: OffsetDateTime,
    val results: List<T>,
    val nextCursor: String?,
    val hasNext: Boolean,
) {
    companion object {
        fun <T : CursorKey> create(
            snapshotAt: OffsetDateTime,
            fetched: List<T>,
            pageSize: Int,
            cursorCodec: CursorCodec,
        ): CursorPageResponse<T> {
            val hasNext = fetched.size > pageSize
            val results = if (hasNext) fetched.take(pageSize) else fetched

            val nextCursor = if (hasNext && results.isNotEmpty()) {
                when (val last = results.last()) {
                    is CompositeCursorKey -> cursorCodec.encode(last.toCursorPayload())
                    else -> cursorCodec.encode(IdCursor(id = last.cursorId))
                }
            } else null

            return CursorPageResponse(
                snapshotAt = snapshotAt,
                results = results,
                nextCursor = nextCursor,
                hasNext = hasNext,
            )
        }
    }
}
