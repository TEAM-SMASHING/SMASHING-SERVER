package org.appjam.smashing.domain.matching.enums

enum class MatchingStatus {
    REQUESTED,       // 매칭 요청
    ACCEPTED,        // 매칭 수락
    REJECTED,        // 매칭 거절
    CANCELLED,       // 매칭 요청 삭제
    COMPLETED        // 매칭 확정
}
