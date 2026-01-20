package org.appjam.smashing.domain.notification.enums

enum class NotificationType {
    // 매칭 관련
    MATCHING_REQUESTED,          // 매칭 신청 도착
    MATCHING_ACCEPTED,           // 매칭 수락됨

    // 상대가 매칭 결과 전송
    MATCHING_RESULT_SUBMITTED,   // 매칭 결과 전송됨

    // 경기 결과 관련
    RESULT_REJECTED_SCORE_MISMATCH,     // 결과 반려 - 점수 오류
    RESULT_REJECTED_WIN_LOSE_REVERSED,  // 결과 반려 - 승패 오류
    RESULT_REJECTED_SCORE_AND_WIN_LOSE_MISMATCH,    // 결과 반려 - 점수 및 승패 오류
    RESULT_REJECTED_GAME_NOT_PLAYED_YET,            // 결과 반려 - 경기 미진행

    // 후기 관련
    REVIEW_RECEIVED               // 후기 도착
}
