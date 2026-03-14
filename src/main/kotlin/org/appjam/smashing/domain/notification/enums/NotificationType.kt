package org.appjam.smashing.domain.notification.enums

enum class NotificationType {
    // 매칭 관련
    MATCHING_REQUESTED,          // 매칭 신청 도착
    MATCHING_ACCEPTED,           // 매칭 수락됨

    // 상대가 매칭 결과 전송
    MATCHING_RESULT_SUBMITTED,   // 매칭 결과 전송됨

    // 경기 관련
    MATCHING_RESULT_REJECTED,    // 경기 결과 반려됨

    // 후기 관련
    REVIEW_RECEIVED               // 후기 도착
}
