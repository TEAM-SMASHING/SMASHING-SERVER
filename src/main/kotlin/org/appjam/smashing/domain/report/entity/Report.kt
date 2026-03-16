package org.appjam.smashing.domain.report.entity

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.appjam.smashing.domain.common.entity.BaseEntity
import org.hibernate.annotations.Comment

@Entity
class Report(
    @Id
    @Tsid
    @Column(length = 13)
    @Comment("신고 IDX")
    val id: String? = null,


    ) : BaseEntity() {
    companion object {

    }
}
