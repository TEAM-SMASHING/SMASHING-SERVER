package org.appjam.smashing.domain.notification.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.game.enums.GameResultRejectReason
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
            template: NotificationTemplate,
            requesterProfile: UserSportProfile,
        ) = Notification(
                params = """{"requesterNickname":"${requesterProfile.user.nickname}","requesterTierName":"${requesterProfile.tier.name}"}""",
                isRead = false,
                linkUrl = "/api/v1/users/me/matchings/received",
                user = receiver,
                notificationTemplate = template,
            )

        fun createMatchingRequestAccepted(
            receiver: User,
            template: NotificationTemplate,
            acceptorProfile: UserSportProfile,
        ) = Notification(
                params = """{"acceptorNickname":"${acceptorProfile.user.nickname}","acceptorTierId":${acceptorProfile.tier.id!!}}""",
                isRead = false,
                linkUrl = "/api/v1/users/me/matchings/accepted/pending-result",
                user = receiver,
                notificationTemplate = template,
            )

        fun createMatchingResultSubmitted(
            receiver: User,
            template: NotificationTemplate,
            gameId: String,
            submissionId: String,
            submitterNickname: String,
            submitterTierId: Long,
        ) = Notification(
            params = """{"submitterNickname":"$submitterNickname","submitterTierId":$submitterTierId,"gameId":"$gameId","submissionId":"$submissionId"}""",
            isRead = false,
            linkUrl = "/api/v1/users/me/matchings/accepted/pending-result",
            user = receiver,
            notificationTemplate = template,
        )

        fun createReviewReceived(
            receiver: User,
            template: NotificationTemplate,
            reviewId: String,
            reviewerNickname: String,
            reviewerTierId: Long,
            gameId: String,
        ) = Notification(
            params = """{"reviewerNickname":"$reviewerNickname","reviewerTierId":$reviewerTierId,"reviewId":"$reviewId","gameId":"$gameId"}""",
            isRead = false,
            linkUrl = "/api/v1/reviews/$reviewId",
            user = receiver,
            notificationTemplate = template,
        )

        fun createResultRejectedScoreMismatch(
            receiver: User,
            template: NotificationTemplate,
            gameId: String,
            submissionId: String,
            rejectorNickname: String,
            rejectorTierId: Long,
        ) = Notification(
            params = """{"rejectorNickname":"$rejectorNickname","rejectorTierId":$rejectorTierId,"gameId":"$gameId","submissionId":"$submissionId","reason":"${GameResultRejectReason.SCORE_MISMATCH.name}"}""",
            isRead = false,
            linkUrl = "/api/v1/users/me/matchings/accepted/pending-result",
            user = receiver,
            notificationTemplate = template,
        )

        fun createResultRejectedWinLoseReversed(
            receiver: User,
            template: NotificationTemplate,
            gameId: String,
            submissionId: String,
            rejectorNickname: String,
            rejectorTierId: Long,
        ) = Notification(
            params = """{"rejectorNickname":"$rejectorNickname","rejectorTierId":$rejectorTierId,"gameId":"$gameId","submissionId":"$submissionId","reason":"${GameResultRejectReason.WIN_LOSE_REVERSED.name}"}""",
            isRead = false,
            linkUrl = "/api/v1/users/me/matchings/accepted/pending-result",
            user = receiver,
            notificationTemplate = template,
        )
    }
}
