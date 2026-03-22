package org.appjam.smashing.domain.notification.dto.response

data class NotificationSportMatchResponse(
    val receiverUserProfileId: String,
    val isMatch: Boolean,
) {
    companion object {
        fun from(
            receiverUserProfileId: String,
            notificationSportCode: String,
            activeSportCode: String,
        ) = NotificationSportMatchResponse(
            receiverUserProfileId = receiverUserProfileId,
            isMatch = notificationSportCode == activeSportCode,
        )
    }
}
