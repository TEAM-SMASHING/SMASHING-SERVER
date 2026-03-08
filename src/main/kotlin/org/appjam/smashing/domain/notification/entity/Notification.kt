package org.appjam.smashing.domain.notification.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.game.entity.Game
import org.appjam.smashing.domain.game.entity.GameResultSubmission
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(
    indexes = [
        Index(name = "idx_notification_user_id", columnList = "user_id"),
        Index(name = "idx_notification_type", columnList = "type"),
        Index(name = "idx_notification_sender_user_id", columnList = "sender_user_id"),
        Index(name = "idx_notification_sender_profile_id", columnList = "sender_profile_id"),
        Index(name = "idx_notification_sport_code", columnList = "sport_code"),
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

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Comment("알림 타입")
    val type: NotificationType? = null,

    @Column(length = 100)
    @Comment("알림 제목")
    val title: String? = null,

    @Column(length = 500)
    @Comment("알림 내용")
    val content: String? = null,

    @Column(name = "sport_code", length = 50)
    @Comment("스포츠 코드")
    val sportCode: String? = null,

    @Column(name = "sender_user_id", length = 13)
    @Comment("발신자 유저 IDX")
    val senderUserId: String? = null,

    @Column(name = "sender_profile_id", length = 13)
    @Comment("발신자 유저-스포츠 프로필 IDX")
    val senderProfileId: String? = null,

    @Column(length = 500)
    @Comment("알림 치환 파라미터(JSON)")
    val params: String? = null, // TODO: 추후 알림 모두 리팩토링 시 삭제

    @Column(nullable = false)
    @Comment("알림 읽음 여부")
    var isRead: Boolean = false,

    @Column(length = 500)
    @Comment("알림 연결 URL")
    val linkUrl: String? = null, // TODO: 추후 알림 모두 리팩토링 시 삭제

    @Column(nullable = false, length = 13)
    @Comment("수신자 유저-스포츠 프로필 IDX")
    val receiverProfileId: String,

    @Column(length = 10)
    @Comment("발신자 유저 닉네임")
    val senderNickname: String? = null, // TODO: senderUserId 기반 조회로 대체 후 제거

    @Comment("수신 스포츠 IDX")
    val receiverSportId: Long? = null, // TODO: 모두 리팩토링시 제거 예정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("수신자 유저 IDX")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_template_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("템플릿 IDX")
    val notificationTemplate: NotificationTemplate? = null, // TODO: 알림 리팩토링 완료 후 제거
) : BaseEntity() {


    companion object {

        /**
         * 매칭 신청 도착 알림 생성
         */
        fun createMatchingRequested(
            receiver: User,
            receiverProfile: UserSportProfile,
            requesterProfile: UserSportProfile,
        ): Notification {
            val sportName = receiverProfile.sport.name
            val sportCode = receiverProfile.sport.code
            val requesterNickname = requesterProfile.user.nickname

            val title = "[$sportName] 매칭 신청이 도착했어요"
            val content = "${requesterNickname}님으로부터 매칭 신청이 도착했어요! 지금 확인해볼까요?"

            return Notification(
                type = NotificationType.MATCHING_REQUESTED,
                title = title,
                content = content,
                sportCode = sportCode,
                senderUserId = requesterProfile.user.id!!,
                senderProfileId = requesterProfile.id!!,
                isRead = false,
                receiverProfileId = receiverProfile.id!!,
                user = receiver,
            )
        }

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
            game : Game,
            submission : GameResultSubmission,
        ) = Notification(
            params = """{"sportName":"${receiverProfile.sport.name}","submitterNickname":"$submitterNickname"}""",
            isRead = false,
            linkUrl = "/api/v1/games/${game.id}/submissions/${submission.id}",
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

        fun createResultRejectedScoreAndWinLoseMismatch(
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
            receiverSportId = receiverProfile.sport.id!!,
            notificationTemplate = template,
        )

        fun createResultRejectedGameNotPlayedYet(
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
            receiverSportId = receiverProfile.sport.id!!,
            notificationTemplate = template,
        )
    }

    fun markAsRead() {
        if (!isRead) {
            isRead = true
        }
    }
}
