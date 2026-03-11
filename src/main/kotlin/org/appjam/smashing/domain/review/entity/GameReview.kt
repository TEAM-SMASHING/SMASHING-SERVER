package org.appjam.smashing.domain.review.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.*
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.appjam.smashing.domain.game.entity.Game
import org.appjam.smashing.domain.review.enums.ReviewRating
import org.appjam.smashing.domain.review.enums.ReviewTag
import org.appjam.smashing.domain.user.entity.User
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(
    indexes = [
        Index(name = "idx_game_review_game_id", columnList = "game_id"),
        Index(name = "idx_game_review_reviewer_profile_id", columnList = "reviewer_profile_id"),
        Index(name = "idx_game_review_reviewee_profile_id", columnList = "reviewee_profile_id"),
    ]
)
@Comment("경기 후기 정보")
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "update game_review set deleted_at = now() where id = ?")
class GameReview(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("경기 후기 IDX")
    val id: String? = null,

    @Column(length = 100)
    @Comment("후기 내용")
    var content: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("경기 만족도(BAD/GOOD/BEST)")
    val rating: ReviewRating,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "game_review_tag",
        joinColumns = [JoinColumn(name = "game_review_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))],
    )
    @Column(name = "tag", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    @Comment("칭찬 태그")
    val tags: MutableSet<ReviewTag> = mutableSetOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "reviewer_user_id",
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("후기 작성자")
    var reviewer: User? = null, // TODO: Review 리팩토링 완료 후 제거 예정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "reviewee_user_id",
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("후기 작성자")
    var reviewee: User? = null, // TODO: Review 리팩토링 완료 후 제거 예정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "reviewer_profile_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("후기 작성자 유저-스포츠 프로필 IDX")
    var reviewerProfile: UserSportProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "reviewee_profile_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("후기 대상 유저-스포츠 프로필 IDX")
    var revieweeProfile: UserSportProfile,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "game_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("경기 IDX")
    var game: Game,
) : BaseEntity() {

    companion object {
        fun create(
            game: Game,
            reviewerProfile: UserSportProfile,
            revieweeProfile: UserSportProfile,
            rating: ReviewRating,
            content: String?,
            tags: Set<ReviewTag>,
        ) = GameReview(
            game = game,
            reviewerProfile = reviewerProfile,
            revieweeProfile = revieweeProfile,
            rating = rating,
            content = content,
            tags = tags.toMutableSet(),
        )
    }
}
