package org.appjam.smashing.global.common.dto

import org.appjam.smashing.global.util.CursorCodec
import java.time.OffsetDateTime

data class CursorResponse<T, M>(
    val snapshotAt: OffsetDateTime,
    val meta: M? = null,
    val results: List<T>,
    val nextCursor: String?,
    val hasNext: Boolean,
) {
    companion object {
        fun <T, M> from(
            page: CursorPageResponse<*>,
            results: List<T>,
            meta: M? = null
        ) = CursorResponse(
            snapshotAt = page.snapshotAt,
            meta = meta,
            results = results,
            nextCursor = page.nextCursor,
            hasNext = page.hasNext,
        )
    }
}

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
                cursorCodec.encode(IdCursor(id = results.last().cursorId))
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
