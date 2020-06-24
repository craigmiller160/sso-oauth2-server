package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import javax.transaction.Transactional

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken,Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken WHERE timestamp < :maxTimestamp")
    fun removeOldTokens(@Param("maxTimestamp") maxTimestamp: LocalDateTime): Int

}