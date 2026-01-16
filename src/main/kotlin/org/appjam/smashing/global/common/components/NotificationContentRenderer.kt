package org.appjam.smashing.global.common.components

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class NotificationContentRenderer(
    private val objectMapper: ObjectMapper,
) {

    private val placeholderRegex = Regex("""\{\{(\w+)\}\}|\{(\w+)\}|\$\{(\w+)\}""")

    fun render(templateContent: String, paramsJson: String): String {
        val params: Map<String, Any?> = runCatching {
            objectMapper.readValue(paramsJson, object : TypeReference<Map<String, Any?>>() {})
        }.getOrElse {
            return templateContent
        }

        return placeholderRegex.replace(templateContent) { match ->
            val key = match.groups[1]?.value
                    ?: match.groups[2]?.value
                    ?: match.groups[3]?.value
                    ?: return@replace match.value

            params[key]?.toString() ?: ""
        }
    }
}
