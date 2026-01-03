package org.appjam.smashing.domain.notification.enums

enum class NotificationType {
    // 매칭 관련
    MATCHING_REQUESTED,          // 매칭 신청 도착
    MATCHING_ACCEPTED,           // 매칭 수락됨

    // 경기 결과 관련
    RESULT_REJECTED_SCORE_MISMATCH,     // 결과 반려 - 점수 오류
    RESULT_REJECTED_WIN_LOSE_REVERSED,  // 결과 반려 - 승패 오류

    // 후기 관련
    REVIEW_RECEIVED               // 후기 도착
}
