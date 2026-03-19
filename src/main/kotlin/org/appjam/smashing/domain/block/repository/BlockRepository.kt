package org.appjam.smashing.domain.block.repository

import org.appjam.smashing.domain.block.entity.Block
import org.springframework.data.jpa.repository.JpaRepository

interface BlockRepository : JpaRepository<Block, String>
