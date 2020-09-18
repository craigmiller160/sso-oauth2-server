/*
 *     Auth Management Service
 *     Copyright (C) 2020 Craig Miller
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.config.TokenConfig
import io.craigmiller160.authserver.repository.RefreshTokenRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RefreshTokenCleanupService (
        private val refreshTokenRepo: RefreshTokenRepository,
        private val tokenConfig: TokenConfig
) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedRate = 1 * 60 * 60 * 1000) // Run every hour
    fun cleanupRefreshTokens() {
        log.info("Cleaning up refresh tokens older than ${tokenConfig.deleteOlderThanSecs} seconds")
        val maxTimestamp = LocalDateTime.now().minusSeconds(tokenConfig.deleteOlderThanSecs)
        refreshTokenRepo.removeOldTokens(maxTimestamp)
    }

}