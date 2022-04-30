/*
 *     sso-oauth2-server
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.isA

@ExtendWith(MockitoExtension::class)
class RefreshTokenCleanupServiceTest {

    @Mock
    private lateinit var refreshTokenRepo: RefreshTokenRepository
    @Mock
    private lateinit var tokenConfig: TokenConfig

    @InjectMocks
    private lateinit var refreshTokenCleanupService: RefreshTokenCleanupService

    @Test
    fun test_cleanupRefreshTokens() {
        `when`(tokenConfig.deleteOlderThanSecs)
                .thenReturn(1000)
        refreshTokenCleanupService.cleanupRefreshTokens()

        verify(refreshTokenRepo, times(1))
                .removeOldTokens(isA())
    }

}