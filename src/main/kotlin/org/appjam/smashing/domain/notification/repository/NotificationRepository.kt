package org.appjam.smashing.domain.notification.repository

import org.appjam.smashing.domain.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface NotificationRepository : JpaRepository<Notification, String>, NotificationRepositoryCustom {

    @Query(
        """
        select n
        from Notification n
        join fetch n.receiverUser u
        where n.id = :notificationId
        """
    )
    fun findByIdFetchUser(
        notificationId: String,
    ): Notification?
}
