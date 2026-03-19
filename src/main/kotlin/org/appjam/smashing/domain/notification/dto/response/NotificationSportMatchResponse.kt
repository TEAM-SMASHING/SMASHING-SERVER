package org.appjam.smashing.domain.notification.dto.response

data class NotificationSportMatchResponse(
    val isMatch: Boolean,
    val senderSportCode: String,
    val activeSportCode: String,
) {
    companion object {
        fun from(
            senderSportCode: String,
            activeSportCode: String,
        ) = NotificationSportMatchResponse(
            isMatch = senderSportCode == activeSportCode,
            senderSportCode = senderSportCode,
            activeSportCode = activeSportCode,
        )
    }
}
