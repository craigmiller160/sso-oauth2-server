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

import io.craigmiller160.authserver.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

  @Query(
    """
        SELECT u 
        FROM User u 
        WHERE u.email = :email
        AND u.id IN (
            SELECT cu.userId
            FROM ClientUser cu
            WHERE cu.clientId = :clientId
        )
    """)
  fun findByEmailAndClientId(email: String, clientId: Long): User?

  @Query(
    """
        SELECT u
        FROM User u
        WHERE u.id = :userId
        AND u.id = (
            SELECT cu.userId
            FROM ClientUser cu
            WHERE cu.clientId = :clientId
            AND cu.userId = :userId
        )
    """)
  fun findByUserIdAndClientId(userId: Long, clientId: Long): User?

  @Query("SELECT u FROM User u WHERE u.id = :userId AND u.enabled = true")
  fun findEnabledUserById(@Param("userId") userId: Long): User?

  @Query("SELECT u FROM User u WHERE u.email = :email AND u.enabled = true")
  fun findEnabledUserByEmail(@Param("email") email: String): User?
}
