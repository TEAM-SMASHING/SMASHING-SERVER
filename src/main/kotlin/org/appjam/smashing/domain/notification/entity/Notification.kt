package org.appjam.smashing.domain.notification.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.game.entity.Game
import org.appjam.smashing.domain.game.entity.GameResultSubmission
import org.appjam.smashing.domain.game.enums.GameSubmissionRejectReason
import org.appjam.smashing.domain.notification.enums.NotificationType
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(
    indexes = [
        Index(name = "idx_notification_user_id", columnList = "receiver_user_id"),
        Index(name = "idx_notification_type", columnList = "type"),
        Index(name = "idx_notification_sender_profile_id", columnList = "sender_profile_id"),
        Index(name = "idx_notification_sport_code", columnList = "sport_code"),
        Index(name = "idx_notification_receiver_user_id_id", columnList = "receiver_user_id, id"),
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

    @Column(length = 13)
    @Comment("발신자 유저 IDX")
    val senderUserId: String? = null,

    @Column(length = 13)
    @Comment("발신자 유저-스포츠 프로필 IDX")
    val senderProfileId: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "receiver_user_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("수신자 유저 IDX")
    val receiverUser: User,

    @Column(nullable = false, length = 13)
    @Comment("수신자 유저-스포츠 프로필 IDX")
    val receiverUserProfileId: String,

    @Column(nullable = false)
    @Comment("알림 읽음 여부")
    var isRead: Boolean = false,

    @Column(length = 500)
    @Comment("알림 연결 URL")
    val linkUrl: String? = null,
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

            val title = "$sportName 매칭 신청이 도착했어요"
            val content = "${requesterNickname}님으로부터 매칭 신청이 도착했어요! 지금 확인해볼까요?"

            return Notification(
                type = NotificationType.MATCHING_REQUESTED,
                title = title,
                content = content,
                sportCode = sportCode,
                senderUserId = requesterProfile.user.id!!,
                senderProfileId = requesterProfile.id!!,
                receiverUserProfileId = receiverProfile.id!!,
                receiverUser = receiver,
            )
        }

        /**
         * 매칭 수락 알림 생성
         */
        fun createMatchingAccepted(
            receiver: User,
            receiverProfile: UserSportProfile,
            acceptorProfile: UserSportProfile,
        ): Notification {
            val sportName = receiverProfile.sport.name
            val sportCode = receiverProfile.sport.code
            val acceptorNickname = acceptorProfile.user.nickname

            val title = "$sportName 매칭이 수락되었어요."
            val content = "${acceptorNickname}님이 내가 보낸 매칭을 수락했어요! 지금 확인해볼까요?"

            return Notification(
                type = NotificationType.MATCHING_ACCEPTED,
                title = title,
                content = content,
                sportCode = sportCode,
                senderUserId = acceptorProfile.user.id!!,
                senderProfileId = acceptorProfile.id!!,
                receiverUserProfileId = receiverProfile.id!!,
                receiverUser = receiver,
            )
        }

        /**
         * 게임 결과 제출 알림 생성
         */
        fun createGameResultSubmitted(
            receiver: User,
            receiverProfile: UserSportProfile,
            submitterProfile: UserSportProfile,
            game: Game,
            submission: GameResultSubmission,
        ): Notification {
            val sportName = receiverProfile.sport.name
            val sportCode = receiverProfile.sport.code
            val submitterNickname = submitterProfile.user.nickname

            val title = "$sportName 매칭 결과가 도착했어요"
            val content = "${submitterNickname}님이 매칭 결과를 보내주셨어요! 지금 확인해볼까요?"

            return Notification(
                type = NotificationType.MATCHING_RESULT_SUBMITTED,
                title = title,
                content = content,
                sportCode = sportCode,
                senderUserId = submitterProfile.user.id!!,
                senderProfileId = submitterProfile.id!!,
                linkUrl = "/api/v1/games/${game.id}/submissions/${submission.id}",
                receiverUserProfileId = receiverProfile.id!!,
                receiverUser = receiver,
            )
        }

        fun createGameResultRejected(
            receiver: User,
            receiverProfile: UserSportProfile,
            rejectorProfile: UserSportProfile,
            reason: GameSubmissionRejectReason,
        ): Notification {
            val sportName = receiverProfile.sport.name
            val sportCode = receiverProfile.sport.code
            val rejectorNickname = rejectorProfile.user.nickname

            val reasonText = when (reason) {
                GameSubmissionRejectReason.WINNER_MISMATCH -> "점수 오류"
                GameSubmissionRejectReason.GAME_NOT_PLAYED_YET -> "경기 미진행"
            }

            return Notification(
                type = NotificationType.MATCHING_RESULT_REJECTED,
                title = "$sportName 매칭 결과가 반려되었어요",
                content = "${rejectorNickname}님이 결과 입력을 거절했어요. (사유: $reasonText)",
                sportCode = sportCode,
                senderUserId = rejectorProfile.user.id!!,
                senderProfileId = rejectorProfile.id!!,
                receiverUserProfileId = receiverProfile.id!!,
                receiverUser = receiver,
            )
        }

        fun createReviewReceived(
            receiver: User,
            receiverProfile: UserSportProfile,
            reviewId: String,
            reviewerProfile: UserSportProfile,
        ): Notification {
            val reviewerNickname = reviewerProfile.user.nickname

            return Notification(
                type = NotificationType.REVIEW_RECEIVED,
                title = "후기가 도착했어요",
                content = "${reviewerNickname}님이 소중한 후기를 보내주셨어요! 지금 확인해볼까요?",
                sportCode = receiverProfile.sport.code,
                senderUserId = reviewerProfile.user.id!!,
                senderProfileId = reviewerProfile.id!!,
                receiverUser = receiver,
                receiverUserProfileId = receiverProfile.id!!,
                linkUrl = "/api/v1/reviews/$reviewId",
            )
        }
    }

    fun markAsRead() {
        if (!isRead) {
            isRead = true
        }
    }
}
