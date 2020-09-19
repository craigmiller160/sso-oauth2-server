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

import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.testutils.TestData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.userdetails.UsernameNotFoundException

@ExtendWith(MockitoExtension::class)
class ClientUserDetailsServiceTest {

    @Mock
    private lateinit var clientRepo: ClientRepository
    private val client = TestData.createClient()

    @InjectMocks
    private lateinit var clientUserDetailsService: ClientUserDetailsService

    @Test
    fun test_loadUserByUsername() {
        val clientKey = "ABC"
        `when`(clientRepo.findByClientKey(clientKey))
                .thenReturn(client)

        val result = clientUserDetailsService.loadUserByUsername(clientKey)
        assertThat(result, allOf(
                hasProperty("enabled", equalTo(client.enabled)),
                hasProperty("username", equalTo(client.clientKey)),
                hasProperty("password", equalTo(client.clientSecret))
        ))
    }

    @Test
    fun test_loadUserByUsername_noClientKey() {
        val ex = assertThrows<UsernameNotFoundException> { clientUserDetailsService.loadUserByUsername(null) }
        assertEquals("No Client Key to lookup", ex.message)
    }

    @Test
    fun test_loadUserByUsername_userNotFound() {
        val ex = assertThrows<UsernameNotFoundException> { clientUserDetailsService.loadUserByUsername("ABC") }
        assertEquals("No client found for Client Key ABC", ex.message)
    }

}
