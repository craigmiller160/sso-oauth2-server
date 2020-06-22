package io.craigmiller160.ssoauthserverexp.repository

import io.craigmiller160.ssoauthserverexp.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken,Long> {

    @Query("DELETE FROM RefreshToken WHERE timestamp < :maxTimestamp")
    fun removeOldTokens(@Param("maxTimestamp") maxTimestamp: LocalDateTime)

}