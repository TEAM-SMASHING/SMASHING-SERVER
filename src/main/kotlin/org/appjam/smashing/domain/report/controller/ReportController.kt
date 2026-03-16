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
        description = "유저를 신고합니다."
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
