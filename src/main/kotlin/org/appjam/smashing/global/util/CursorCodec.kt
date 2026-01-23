package org.appjam.smashing.global.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.appjam.smashing.global.common.dto.CursorPayload
import org.appjam.smashing.global.common.dto.IdCursor
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class CursorCodec(
    private val objectMapper: ObjectMapper,
) {
    fun encode(
        cursor: CursorPayload
    ): String {
        val json = objectMapper.writeValueAsString(cursor)
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(json.toByteArray(StandardCharsets.UTF_8))
    }

    fun decode(
        cursor: String?
    ): IdCursor? {
        if (cursor.isNullOrBlank()) return null

        val json = String(
            Base64.getUrlDecoder().decode(cursor),
            StandardCharsets.UTF_8,
        )

        return objectMapper.readValue(json, IdCursor::class.java)
    }

    fun <T> decode(
        cursor: String?,
        clazz: Class<T>,
    ): T? {
        if (cursor.isNullOrBlank()) return null

        val json = String(
            Base64.getUrlDecoder().decode(cursor),
            StandardCharsets.UTF_8,
        )

        return objectMapper.readValue(json, clazz)
    }
}
