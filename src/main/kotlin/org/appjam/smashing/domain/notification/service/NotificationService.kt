package org.appjam.smashing.domain.notification.service

import org.appjam.smashing.domain.notification.entity.Notification
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.notification.repository.NotificationRepository
import org.appjam.smashing.domain.notification.repository.NotificationTemplateRepository
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.appjam.smashing.global.exception.CustomException
import org.appjam.smashing.global.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val notificationTemplateRepository: NotificationTemplateRepository,
) {

    fun createMatchingRequested(
        receiver: User,
        requesterProfile: UserSportProfile,
    ): Notification {
        val template = notificationTemplateRepository.findByType(NotificationType.MATCHING_REQUESTED)
            ?: throw CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND)

        return notificationRepository.save(
            Notification.createMatchingRequested(
                receiver = receiver,
                template = template,
                requesterProfile = requesterProfile,
            )
        )
    }

    fun createMatchingAccepted(
        receiver: User,
        acceptorProfile: UserSportProfile,
    ): Notification {
        val template = notificationTemplateRepository.findByType(NotificationType.MATCHING_ACCEPTED)
            ?: throw CustomException(ErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND)

        return notificationRepository.save(
            Notification.createMatchingRequestAccepted(
                receiver = receiver,
                template = template,
                acceptorProfile = acceptorProfile,
            )
        )
    }
}
