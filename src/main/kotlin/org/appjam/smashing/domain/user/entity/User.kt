package org.appjam.smashing.domain.user.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.user.enums.Gender
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(
    indexes = [
        Index(name = "idx_nickname", columnList = "nickname")
    ]
)
@Comment("유저 정보")
@SQLRestriction("deleted_at is null")
@SQLDelete(
    sql = """
        update user 
        set deleted_at = now()
        where id = ?
        """
)
class User(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("유저 IDX")
    val id: String? = null,

    @Column(nullable = false)
    @Comment("카카오 IDX")
    val kakaoId: String,

    @Column(nullable = false, length = 50)
    @Comment("닉네임")
    val nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(30)")
    @Comment("성별")
    val gender: Gender,

    @Column(nullable = false, columnDefinition = "TEXT")
    @Comment("오픈채팅 링크")
    val openchatUrl: String,

    @Column(nullable = false)
    @Comment("지역")
    var region: String,

    @Column(name = "active_user_sport_profile_id", length = 13)
    @Comment("현재 활성화된 유저-스포츠 프로필 IDX")
    var activeUserSportProfileId: String? = null,

    @Column
    @Comment("제재 종료 일시")
    var restrictionEndDate: LocalDateTime? = null
) : BaseEntity() {

    fun updateActiveProfile(
        profileId: String,
    ) {
        this.activeUserSportProfileId = profileId
    }

    fun updateRegion(
        newRegion: String,
    ) {
        this.region = newRegion
    }

    fun applyRestriction(
        durationDays: Long,
    ) {
        this.restrictionEndDate = LocalDateTime.now().plusDays(durationDays)
    }

    fun isRestricted(now: LocalDateTime = LocalDateTime.now()): Boolean = restrictionEndDate?.isAfter(now) ?: false

    companion object {
        const val DELETED_USER_NICKNAME = "알 수 없음"

        fun create(
            kakaoId: String,
            nickname: String,
            gender: Gender,
            openchatUrl: String,
            region: String,
        ) = User(
            kakaoId = kakaoId,
            nickname = nickname,
            gender = gender,
            openchatUrl = openchatUrl,
            region = region,
        )
    }

}
