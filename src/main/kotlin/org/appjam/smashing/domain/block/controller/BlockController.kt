package org.appjam.smashing.domain.block.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.appjam.smashing.domain.block.dto.request.UserBlockRequest
import org.appjam.smashing.domain.block.service.BlockService
import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Block")
@RestController
@RequestMapping("/api/v1/blocks")
class BlockController(
    private val blockService: BlockService,
) {
    @Operation(
        summary = "차단 API",
        description = """
            유저를 차단합니다.
        """
    )
    @PostMapping
    fun blockUser(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid @RequestBody userBlockRequest: UserBlockRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        blockService.blockUser(
            userId = principal.username,
            requestCommand = userBlockRequest.toCommand(),
        )

        return ApiResponse.success()
    }
}
