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
            select usp 
            from UserSportProfile usp
            join fetch usp.sport s
            join fetch usp.tier 
            where usp.user.id = : userId
            order by s.name asc
        """
    )
    fun findAllByUserId(
        userId: String,
    ): List<UserSportProfile>
}
