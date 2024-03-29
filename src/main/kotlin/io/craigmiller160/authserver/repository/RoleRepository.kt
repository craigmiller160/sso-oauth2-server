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

import io.craigmiller160.authserver.entity.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role, Long> {

  @Query(
    """
        SELECT r
        FROM Role r
        WHERE r.id IN (
            SELECT cur.roleId
            FROM ClientUserRole cur
            WHERE cur.userId = :userId
            AND cur.clientId = :clientId
        )
    """)
  fun findAllByUserIdAndClientId(
    @Param("userId") userId: Long,
    @Param("clientId") clientId: Long
  ): List<Role>

  @Query(
    """
        SELECT r
        FROM Role r
        WHERE r.id IN (
            SELECT cur.roleId
            FROM ClientUserRole cur
            WHERE cur.userId = :userId
        )
    """)
  fun findAllByUserId(@Param("userId") userId: Long): List<Role>
}
