/*
 *     SSO OAuth2 Auth Server
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

import io.craigmiller160.authserver.entity.ClientRedirectUri
import io.craigmiller160.authserver.exception.AuthCodeException
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.testutils.TestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class UIServiceTest {

    @Mock
    private lateinit var clientRepo: ClientRepository

    @InjectMocks
    private lateinit var uiService: UIService

    @Test
    fun test_validateRequest() {
        val request = TestData.createPageRequest()
        val client = TestData.createClient()
                .copy(clientRedirectUris = listOf(ClientRedirectUri(0, 0, "http://somewhere.com/authcode/code")))

        `when`(clientRepo.findByClientKey(request.client_id))
                .thenReturn(client)

        uiService.validateRequest(request)
        // No tests needed if an exception is not thrown
    }

    @Test
    fun test_validateRequest_invalidResponseType() {
        val request = TestData.createPageRequest().copy(response_type = "other")

        val ex = assertThrows<AuthCodeException> { uiService.validateRequest(request) }
        assertEquals("Invalid response type", ex.message)
    }

    @Test
    fun test_validateRequest_badClient() {
        val request = TestData.createPageRequest()

        val ex = assertThrows<AuthCodeException> { uiService.validateRequest(request) }
        assertEquals("Client not supported", ex.message)
    }

    @Test
    fun test_validateRequest_authCodeNotAllowed() {
        val request = TestData.createPageRequest()

        `when`(clientRepo.findByClientKey(request.client_id))
                .thenReturn(TestData.createClient().copy(clientRedirectUris = listOf(ClientRedirectUri(0, 0, ""))))

        val ex = assertThrows<AuthCodeException> { uiService.validateRequest(request) }
        assertEquals("Client does not support Auth Code", ex.message)
    }

}
