package org.appjam.smashing.domain.user.repository

import jakarta.persistence.LockModeType
import org.appjam.smashing.domain.user.entity.UserSportProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface UserSportProfileRepository : JpaRepository<UserSportProfile, String> {

    @Query(
        """
        select usp
          from UserSportProfile usp
          join fetch usp.user u
          join fetch usp.tier t
          join fetch usp.sport s
         where u.id = :userId
           and s.id = :sportId
        """
    )
    fun findByUserIdAndSportIdFetch(
        userId: String,
        sportId: Long,
    ): UserSportProfile?

    @Query(
        """
    select usp
      from UserSportProfile usp
      join fetch usp.user
      join fetch usp.sport
      join fetch usp.tier
     where usp.id = :profileId
    """
    )
    fun findByIdFetchAll(
        profileId: String,
    ): UserSportProfile?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        select usp
          from UserSportProfile usp
          join fetch usp.user
          join fetch usp.sport
          join fetch usp.tier
         where usp.user.id = :userId
           and usp.sport.id = :sportId
        """
    )
    fun findByUserIdAndSportIdForUpdate(
        userId: String,
        sportId: Long,
    ): UserSportProfile?

    @Query(
        """
        select usp.id
        from UserSportProfile usp
        where usp.user.id = :userId
          and usp.sport.id = :sportId
        """
    )
    fun findProfileIdByUserIdAndSportId(
        userId: String,
        sportId: Long,
    ): String?

    @Query(
        """
        select usp.tier.id
        from UserSportProfile usp
        where usp.user.id = :userId
          and usp.sport.id = :sportId
        """
    )
    fun findTierIdByUserIdAndSportId(
        userId: String,
        sportId: Long,
    ): Long?

    @Query(
        """
            select usp 
            from UserSportProfile usp
            join fetch usp.sport s
            join fetch usp.tier 
            where usp.user.id = :userId
            order by s.name asc
        """
    )
    fun findAllByUserIdOrderByName(
        userId: String,
    ): List<UserSportProfile>

    fun existsByUserIdAndSportId(userId: String, sportId: Long): Boolean

    @Query(
        """
            select usp
            from UserSportProfile usp
            join fetch usp.sport s
            join fetch usp.tier
            where usp.user.id = :userId
            """
    )
    fun findAllByUserId(
        userId: String,
    ): List<UserSportProfile>

    @Query(
        """
            select usp
            from UserSportProfile usp
            join fetch usp.user u
            join fetch usp.sport s
            join fetch usp.tier t
            where u.region = :region
              and s.id = :sportId
              and u.id <> :excludeUserId
            order by u.id asc
        """
    )
    fun findAllByRegionAndSportOrderByUserId(
        region: String,
        sportId: Long,
        excludeUserId: String
    ): List<UserSportProfile>

    @Query(
        """
            select usp
            from UserSportProfile usp
            join fetch usp.user u
            join fetch usp.sport s
            join fetch usp.tier t
            where u.region = :region
              and s.id = :sportId
              and u.id <> :excludeUserId
            order by usp.lp desc, u.nickname asc 
            limit 30
        """
    )
    fun findAllByRegionAndSportOrderByLp(
        region: String,
        sportId: Long,
        excludeUserId: String
    ): List<UserSportProfile>
}
