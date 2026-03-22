package org.appjam.smashing.domain.report.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.appjam.smashing.domain.report.dto.request.UserReportRequest
import org.appjam.smashing.domain.report.service.ReportService
import org.appjam.smashing.global.auth.security.data.CustomUserDetails
import org.appjam.smashing.global.common.dto.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Report")
@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    private val reportService: ReportService,
) {
    @Operation(
        summary = "신고 API",
        description = """
            유저를 신고합니다.
                
            [정책]
             - 30일 내에 서로 다른 3명의 유저에게 신고당할 경우, 7일 동안 탐색/추천에서 노출에 제외됩니다.
             - 같은 유저를 두 번 이상 신고할 수 없습니다.
               단, 30일 이후에 동일 유저 신고 가능
        """
    )
    @PostMapping
    fun reportUser(
        @AuthenticationPrincipal principal: CustomUserDetails,
        @Valid @RequestBody userReportRequest: UserReportRequest,
    ): ResponseEntity<ApiResponse<Unit>> {
        reportService.reportUser(
            userId = principal.username,
            requestCommand = userReportRequest.toCommand()
        )

        return ApiResponse.success()
    }
}
