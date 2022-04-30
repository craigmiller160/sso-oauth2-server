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

import io.craigmiller160.authserver.testutils.TestData
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
  private val client = TestData.createClient()

  @BeforeEach
  fun setup() {
    clientRepo.save(client)
  }

  @AfterEach
  fun clean() {
    clientRepo.deleteAll()
  }

  @Test
  fun test_findByClientKey() {
    val result = clientRepo.findByClientKey("Key")
    assertNotNull(result)
    assertEquals(client.clientSecret, result!!.clientSecret)
  }

  @Test
  fun test_findByClientKey_noResults() {
    val result = clientRepo.findByClientKey("abc")
    assertNull(result)
  }
}
