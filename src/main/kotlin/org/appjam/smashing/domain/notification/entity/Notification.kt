package org.appjam.smashing.domain.notification.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(
    indexes = [
        Index(name = "idx_notification_user_id", columnList = "user_id"),
        Index(name = "idx_notification_template_id", columnList = "notification_template_id"),
    ]
)
@Comment("알림 정보")
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "update notification set deleted_at = now() where id = ?")
class Notification(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("알림 IDX")
    val id: String? = null,

    @Column(nullable = false, length = 500)
    @Comment("알림 치환 파라미터(JSON)")
    val params: String,

    @Column(nullable = false)
    @Comment("알림 읽음 여부")
    var isRead: Boolean = false,

    @Column(nullable = false, length = 500)
    @Comment("알림 연결 URL(라우팅 경로)")
    val linkUrl: String,

    @Column(nullable = false, length = 13)
    @Comment("수신자 유저-스포츠 프로필 IDX")
    val receiverProfileId: String,

    @Column(length = 10)
    @Comment("발신자 유저 닉네임")
    val senderNickname: String,

    @Column(nullable = false)
    @Comment("수신 스포츠 IDX")
    val receiverSportId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("수신자 유저 IDX")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "notification_template_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("템플릿 IDX")
    val notificationTemplate: NotificationTemplate,
) : BaseEntity() {

    companion object {
        fun createMatchingRequested(
            receiver: User,
            receiverProfile: UserSportProfile,
            template: NotificationTemplate,
            requesterProfile: UserSportProfile,
        ) = Notification(
            params = """{"sportName":"${receiverProfile.sport.name}","requesterNickname":"${requesterProfile.user.nickname}","requesterTierName":"${requesterProfile.tier.name}"}""",
            isRead = false,
                linkUrl = "/api/v1/users/me/matchings/received",
                receiverProfileId = receiverProfile.id!!,
                receiverSportId = receiverProfile.sport.id!!,
                senderNickname = requesterProfile.user.nickname,
                user = receiver,
                notificationTemplate = template,
            )

        fun createMatchingRequestAccepted(
            receiver: User,
            receiverProfile: UserSportProfile,
            template: NotificationTemplate,
            acceptorProfile: UserSportProfile,
        ) = Notification(
                params = """{"sportName":"${receiverProfile.sport.name}","acceptorNickname":"${acceptorProfile.user.nickname}","acceptorTierName":"${acceptorProfile.tier.name}"}""",
                isRead = false,
                linkUrl = "/api/v1/users/me/games/pending-results",
                receiverProfileId = receiverProfile.id!!,
                senderNickname = acceptorProfile.user.nickname,
                receiverSportId = receiverProfile.sport.id!!,
                user = receiver,
                notificationTemplate = template,
            )

        fun createMatchingResultSubmitted(
            receiver: User,
            receiverProfile: UserSportProfile,
            template: NotificationTemplate,
            submitterNickname: String,
        ) = Notification(
            params = """{"sportName":"${receiverProfile.sport.name}","submitterNickname":"$submitterNickname"}""",
            isRead = false,
            linkUrl = "/api/v1/users/me/games/pending-results",
            user = receiver,
            senderNickname = submitterNickname,
            receiverProfileId = receiverProfile.id!!, // TODO: 발신자 프로필 ID 추가
            receiverSportId = receiverProfile.sport.id!!,
            notificationTemplate = template,
        )

        fun createReviewReceived(
            receiver: User,
            receiverProfile: UserSportProfile,
            template: NotificationTemplate,
            reviewId: String,
            reviewerNickname: String,
        ) = Notification(
            params = """{"reviewerNickname":"$reviewerNickname"}""",
            isRead = false,
            linkUrl = "/api/v1/reviews/$reviewId",
            user = receiver,
            senderNickname = reviewerNickname,
            receiverProfileId = receiverProfile.id!!, // TODO: 발신자 프로필 ID 추가
            receiverSportId = receiverProfile.sport.id!!,
            notificationTemplate = template,
        )

        fun createResultRejectedScoreMismatch(
            receiver: User,
            receiverProfile: UserSportProfile,
            template: NotificationTemplate,
            rejectorNickname: String,
        ) = Notification(
            params = """{"sportName":"${receiverProfile.sport.name}","rejectorNickname":"$rejectorNickname"}""",
            isRead = false,
            linkUrl = "/api/v1/users/me/games/pending-results",
            user = receiver,
            senderNickname = rejectorNickname,
            receiverProfileId = receiverProfile.id!!, // TODO: 발신자 프로필 ID 추가
            receiverSportId = receiverProfile.sport.id!!,
            notificationTemplate = template,
        )

        fun createResultRejectedWinLoseReversed(
            receiver: User,
            receiverProfile: UserSportProfile,
            template: NotificationTemplate,
            rejectorNickname: String,
        ) = Notification(
            params = """{"sportName":"${receiverProfile.sport.name}","rejectorNickname":"$rejectorNickname"}""",
            isRead = false,
            linkUrl = "/api/v1/users/me/games/pending-results",
            user = receiver,
            senderNickname = rejectorNickname,
            receiverProfileId = receiverProfile.id!!,
            receiverSportId = receiverProfile.sport.id!!, // TODO: 발신자 프로필 ID 추가
            notificationTemplate = template,
        )
    }

    fun markAsRead() {
        if (!isRead) {
            isRead = true
        }
    }
}
