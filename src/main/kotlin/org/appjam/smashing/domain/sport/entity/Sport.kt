package org.appjam.smashing.domain.sport.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.Comment

@Entity
@Comment("스포츠 정보")
class Sport(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("스포츠 IDX")
    val id: Long? = null,

    @Column(nullable = false, length = 50)
    @Comment("스포츠 코드")
    val code: String,

    @Column(nullable = false, length = 50)
    @Comment("스포츠 이름")
    val name: String,
)
