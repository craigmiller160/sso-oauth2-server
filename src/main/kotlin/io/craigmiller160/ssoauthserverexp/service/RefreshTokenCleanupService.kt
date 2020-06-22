package io.craigmiller160.ssoauthserverexp.service

import io.craigmiller160.ssoauthserverexp.repository.RefreshTokenRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RefreshTokenCleanupService (
        private val refreshTokenRepo: RefreshTokenRepository
) {

    @Scheduled(fixedRate = 1 * 60 * 60 * 1000) // Run every hour
    fun cleanupRefreshTokens() {
        val maxTimestamp = LocalDateTime.now().minusDays(1)
        refreshTokenRepo.removeOldTokens(maxTimestamp)
    }

}