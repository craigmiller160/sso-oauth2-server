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

import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.ClientUser
import io.craigmiller160.authserver.entity.ClientUserRole
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.testutils.TestData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class RoleRepositoryTest {

  @Autowired private lateinit var userRepo: UserRepository
  @Autowired private lateinit var clientRepo: ClientRepository
  @Autowired private lateinit var roleRepo: RoleRepository
  @Autowired private lateinit var clientUserRoleRepo: ClientUserRoleRepository
  @Autowired private lateinit var clientUserRepo: ClientUserRepository

  private lateinit var user: User
  private lateinit var client: Client
  private lateinit var client2: Client
  private lateinit var clientUser: ClientUser
  private lateinit var clientUser2: ClientUser
  private lateinit var role1: Role
  private lateinit var role2: Role
  private lateinit var role3: Role
  private lateinit var clientUserRole1: ClientUserRole
  private lateinit var clientUserRole2: ClientUserRole
  private lateinit var clientUserRole3: ClientUserRole

  @BeforeEach
  fun setup() {
    user = userRepo.save(TestData.createUser())
    client = clientRepo.save(TestData.createClient())
    client2 =
        clientRepo.save(TestData.createClient().copy(name = "FooBar", clientKey = "FooBarKey"))
    clientUser = clientUserRepo.save(TestData.createClientUser(user.id, client.id))
    clientUser2 = clientUserRepo.save(TestData.createClientUser(user.id, client2.id))
    role1 = roleRepo.save(TestData.createRole1(client.id))
    role2 = roleRepo.save(TestData.createRole2(client.id))
    role3 = roleRepo.save(TestData.createRole3(client2.id))
    clientUserRole1 =
        clientUserRoleRepo.save(TestData.createClientUserRole(user.id, client.id, role1.id))
    clientUserRole2 =
        clientUserRoleRepo.save(TestData.createClientUserRole(user.id, client.id, role2.id))
    clientUserRole3 =
        clientUserRoleRepo.save(TestData.createClientUserRole(user.id, client2.id, role3.id))
  }

  @AfterEach
  fun clean() {
    clientUserRoleRepo.deleteAll()
    roleRepo.deleteAll()
    clientRepo.deleteAll()
    userRepo.deleteAll()
  }

  @Test
  fun test_findAllByUserIdAndClientId() {
    val results = roleRepo.findAllByUserIdAndClientId(user.id, client.id)
    assertEquals(2, results.size)
    assertTrue(results.contains(role1))
    assertTrue(results.contains(role2))
  }

  @Test
  fun test_findAllByUserId() {
    val results = roleRepo.findAllByUserId(user.id)
    assertThat(results, allOf(hasSize(3), containsInAnyOrder(role1, role2, role3)))
  }
}
