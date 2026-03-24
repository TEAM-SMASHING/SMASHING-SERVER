package org.appjam.smashing.domain.notification.dto.response

data class NotificationSportMatchResponse(
    val receiverUserProfileId: String,
    val notificationSportCode: String,
    val isMatch: Boolean,
) {
    companion object {
        fun from(
            receiverUserProfileId: String,
            notificationSportCode: String,
            activeSportCode: String,
        ) = NotificationSportMatchResponse(
            receiverUserProfileId = receiverUserProfileId,
            notificationSportCode = notificationSportCode,
            isMatch = notificationSportCode == activeSportCode,
        )
    }
}
