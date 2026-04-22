package org.appjam.smashing.domain.auth.social

import com.fasterxml.jackson.databind.JsonNode
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.concurrent.TimeUnit

@Component
class OidcJwksClient(
    private val restTemplate: RestTemplate,
) {
    private val cache: Cache<String, JsonNode> = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .maximumSize(10)
        .build()

    fun getKeys(
        jwksUri: String,
    ): JsonNode = cache.get(jwksUri) {
        restTemplate.getForObject(jwksUri, JsonNode::class.java)
            ?: throw CustomException(ErrorCode.INVALID_ID_TOKEN)
    } ?: throw CustomException(ErrorCode.INVALID_ID_TOKEN)

}
