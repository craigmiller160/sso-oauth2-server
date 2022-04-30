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
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.testutils.TestData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class ClientRepositoryTest {

  @Autowired private lateinit var clientRepo: ClientRepository
  @Autowired private lateinit var clientUserRepo: ClientUserRepository
  @Autowired private lateinit var userRepo: UserRepository
  private lateinit var client1: Client
  private lateinit var client2: Client
  private lateinit var client3: Client
  private lateinit var user: User
  private lateinit var clientUser1: ClientUser
  private lateinit var clientUser2: ClientUser
  private lateinit var clientUser3: ClientUser

  @BeforeEach
  fun setup() {
    client1 = clientRepo.save(TestData.createClient())
    client2 =
      clientRepo.save(TestData.createClient().copy(name = "FooBar", clientKey = "FooBarKey"))
    client3 =
      clientRepo.save(
        TestData.createClient().copy(name = "abc", clientKey = "abcKey", enabled = false))
    user = userRepo.save(TestData.createUser())
    clientUser1 = clientUserRepo.save(ClientUser(id = 1, userId = user.id, clientId = client1.id))
    clientUser2 = clientUserRepo.save(ClientUser(id = 2, userId = user.id, clientId = client2.id))
    clientUser3 = clientUserRepo.save(ClientUser(id = 3, userId = user.id, clientId = client3.id))
  }

  @AfterEach
  fun clean() {
    clientUserRepo.deleteAll()
    clientRepo.deleteAll()
    userRepo.deleteAll()
  }

  @Test
  fun test_findByClientKey() {
    val result = clientRepo.findByClientKey("Key")
    assertNotNull(result)
    assertEquals(client1.clientSecret, result!!.clientSecret)
  }

  @Test
  fun test_findByClientKey_noResults() {
    val result = clientRepo.findByClientKey("abc")
    assertNull(result)
  }

  @Test
  fun test_findAllEnabledClientsByUserId() {
    val result = clientRepo.findAllEnabledClientsByUserId(user.id)
    assertEquals(2, result.size)
    assertThat(result, allOf(hasSize(2), containsInAnyOrder(client1, client2)))
  }
}
