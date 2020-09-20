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

package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.RefreshToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDateTime

@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired
    private lateinit var refreshTokenRepo: RefreshTokenRepository

    private lateinit var token1: RefreshToken
    private lateinit var token2: RefreshToken
    private lateinit var token3: RefreshToken
    private lateinit var token4: RefreshToken

    @BeforeEach
    fun setup() {
        token1 = RefreshToken("1", "ABC", 1, null, LocalDateTime.of(2020, 1, 1, 1, 1))
        token2 = RefreshToken("2", "DEF", 2, 1, LocalDateTime.of(2020, 2, 2, 2, 2))
        token3 = RefreshToken("3", "GHI", 2, 2, LocalDateTime.now())
        token4 = RefreshToken("4", "JKL", 1, 1, LocalDateTime.now())

        token1 = refreshTokenRepo.save(token1)
        token2 = refreshTokenRepo.save(token2)
        token3 = refreshTokenRepo.save(token3)
        token4 = refreshTokenRepo.save(token4)
    }

    @AfterEach
    fun clean() {
        refreshTokenRepo.deleteAll()
    }

    @Test
    fun test_removeOldTokens() {
        val maxTimestamp = LocalDateTime.now().minusDays(1)
        val result = refreshTokenRepo.removeOldTokens(maxTimestamp)
        assertEquals(2, result)

        val remaining = refreshTokenRepo.findAll()
        assertEquals(2, remaining.size)
        assertEquals(token3, remaining[0])
        assertEquals(token4, remaining[1])
    }

    @Test
    fun test_removeClientOnlyRefresh() {
        val result = refreshTokenRepo.removeClientOnlyRefresh(1)
        assertEquals(1, result)
        val count = refreshTokenRepo.count()
        assertEquals(3, count)

        val remaining = refreshTokenRepo.findById("4").get()
        assertEquals(token4, remaining)
    }

    @Test
    fun test_removeClientUserRefresh() {
        val result = refreshTokenRepo.removeClientUserRefresh(2, 1)
        assertEquals(1, result)
        val count = refreshTokenRepo.count()
        assertEquals(3, count)

        val remaining = refreshTokenRepo.findById("3").get()
        assertEquals(token3, remaining)
    }

}