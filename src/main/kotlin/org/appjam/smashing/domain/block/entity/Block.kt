package org.appjam.smashing.domain.block.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.hibernate.annotations.Comment

@Entity
@Table(
    name = "block"
)
@Comment("차단 정보")
class Block(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("차단 IDX")
    val id: String? = null,
) : BaseEntity() {
    companion object {
        fun create() {

        }
    }
}
