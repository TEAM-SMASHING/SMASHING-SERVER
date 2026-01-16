package org.appjam.smashing.domain.notification.controller

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.appjam.smashing.domain.notification.dto.response.NotificationSummaryResponse
import org.appjam.smashing.domain.notification.service.NotificationService
import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.appjam.smashing.global.common.dto.ApiResponse
import org.appjam.smashing.global.common.dto.CommonCursorRequest
import org.appjam.smashing.global.common.dto.CursorResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

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
        @AuthenticationPrincipal principal: CustomUserDetails,
        @PathVariable notificationId: String,
    ): ResponseEntity<ApiResponse<Unit>> {
        notificationService.markAsRead(
            userId = principal.username,
            notificationId = notificationId,
        )

        return ApiResponse.success()
    }

    @Operation(
        summary = "받은 알림 목록 조회 API",
        description = """
            사용자가 받은 알림 목록을 조회합니다.
            - 정렬 기준
             - 최신순: notification.id DESC (TSID 기반 최신순)
            - 페이징 방식
             - cursor(keyset) 기반 페이징
             - nextCursor는 "마지막 알림 id"를 기준으로 생성됩니다.
             - 다음 페이지 요청 시 cursor에 nextCursor.
            - 스냅샷 고정
            - 최초 요청에서 snapshotAt이 없으면 서버가 now로 고정하여 내려줍니다.
            - 다음 페이지 요청부터는 응답의 snapshotAt을 그대로 재전송.
        """
    )
    @GetMapping("/me")
    fun getMyNotifications(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid request: CommonCursorRequest,
    ): ResponseEntity<ApiResponse<CursorResponse<NotificationSummaryResponse>>> {
        val response = notificationService.getMyNotifications(
            userId = principal.username,
            request = request,
        )

        return ApiResponse.success(response)
    }
}
