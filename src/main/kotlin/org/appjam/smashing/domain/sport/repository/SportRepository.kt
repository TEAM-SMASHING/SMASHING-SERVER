package org.appjam.smashing.domain.sport.repository

import org.appjam.smashing.domain.sport.entity.Sport
import org.springframework.data.jpa.repository.JpaRepository

interface SportRepository : JpaRepository<Sport, Long> {
    fun findByCode(code: String): Sport?
}
