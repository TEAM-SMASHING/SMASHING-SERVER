package org.appjam.smashing.domain.notification.repository

import org.appjam.smashing.domain.notification.entity.NotificationTemplate
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationTemplateRepository : JpaRepository<NotificationTemplate, Long> {
    fun findByType(type: NotificationType): NotificationTemplate?
}
