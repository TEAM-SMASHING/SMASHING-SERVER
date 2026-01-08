package org.appjam.smashing.domain.notification.repository

import org.appjam.smashing.domain.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, String>
