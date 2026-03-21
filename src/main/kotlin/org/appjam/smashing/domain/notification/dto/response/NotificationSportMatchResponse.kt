package org.appjam.smashing.domain.notification.dto.response

data class NotificationSportMatchResponse(
    val isMatch: Boolean,
    val notificationSportCode: String,
    val activeSportCode: String,
) {
    companion object {
        fun from(
            notificationSportCode: String,
            activeSportCode: String,
        ) = NotificationSportMatchResponse(
            isMatch = notificationSportCode == activeSportCode,
            notificationSportCode = notificationSportCode,
            activeSportCode = activeSportCode,
        )
    }
}
