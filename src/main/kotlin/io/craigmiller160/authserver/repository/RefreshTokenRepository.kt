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
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import javax.transaction.Transactional

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken,String> {

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken WHERE timestamp < :maxTimestamp")
    fun removeOldTokens(@Param("maxTimestamp") maxTimestamp: LocalDateTime): Int

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken WHERE clientId = :clientId AND userId IS NULL")
    fun removeClientOnlyRefresh(@Param("clientId") clientId: Long): Int

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken WHERE clientId = :clientId AND userId = :userId")
    fun removeClientUserRefresh(@Param("clientId") clientId: Long, @Param("userId") userId: Long): Int

}