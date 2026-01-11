package org.appjam.smashing.domain.notification.controller

import io.swagger.v3.oas.annotations.Operation
import org.appjam.smashing.domain.notification.service.NotificationService
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService,
) {

    @Operation(
        summary = "알림 단건 읽음 처리 API",
        description = """
            사용자가 받은 알림 1건을 읽음 처리합니다.
            - isRead = true로 변경
            - 본인 알림만 처리 가능
        """
    )
    @PutMapping("/{notificationId}/read")
    fun readNotification(
        @RequestHeader("userId") userId: String, // TODO: 인증/인가 적용 시 변경
        @PathVariable notificationId: String,
    ): ResponseEntity<ApiResponse<Unit>> {
        notificationService.markAsRead(
            userId = userId,
            notificationId = notificationId,
        )

        return ApiResponse.success()
    }
}
