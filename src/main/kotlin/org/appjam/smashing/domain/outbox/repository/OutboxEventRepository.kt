package org.appjam.smashing.domain.outbox.repository

import org.appjam.smashing.domain.outbox.entity.OutboxEvent
import org.appjam.smashing.domain.outbox.enums.OutboxEventStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface OutboxEventRepository : JpaRepository<OutboxEvent, String> {

    @Query("""
    select e.id
      from OutboxEvent e
     where e.userId = :userId
       and e.status = :status
     order by e.id asc
     """)
    fun findIdsByUserIdAndStatus(
        userId: String,
        status: OutboxEventStatus,
        pageable: Pageable,
    ): List<String>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update OutboxEvent e
           set e.status = :processing,
               e.lastAttemptAt = :now
         where e.id = :id
           and e.status = :pending
    """)
    fun markProcessingIfPending(
        id: String,
        pending: OutboxEventStatus = OutboxEventStatus.PENDING,
        processing: OutboxEventStatus = OutboxEventStatus.PROCESSING,
        now: LocalDateTime,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update OutboxEvent e
           set e.status = :pending,
               e.lastAttemptAt = :now
         where e.id = :id
           and e.status = :processing
    """)
    fun markPendingIfProcessing(
        id: String,
        pending: OutboxEventStatus = OutboxEventStatus.PENDING,
        processing: OutboxEventStatus = OutboxEventStatus.PROCESSING,
        now: LocalDateTime,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update OutboxEvent e
           set e.status = :failed,
               e.lastAttemptAt = :now
         where e.id = :id
           and e.status = :processing
    """)
    fun markFailedIfProcessing(
        id: String,
        failed: OutboxEventStatus = OutboxEventStatus.FAILED,
        processing: OutboxEventStatus = OutboxEventStatus.PROCESSING,
        now: LocalDateTime,
    ): Int
}
