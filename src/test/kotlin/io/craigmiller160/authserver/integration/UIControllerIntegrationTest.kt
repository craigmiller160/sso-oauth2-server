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

package io.craigmiller160.authserver.integration

import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.ClientRedirectUri
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.testutils.TestData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class UIControllerIntegrationTest : AbstractControllerIntegrationTest() {

    @Autowired
    private lateinit var clientRepo: ClientRepository

    private lateinit var client2: Client

    @BeforeEach
    fun setup() {
        client2 = TestData.createClient().copy(
                clientKey = "key2",
                clientRedirectUris = listOf()
        )

        client2 = clientRepo.save(client2)
    }

    @AfterEach
    fun clean() {
        clientRepo.deleteAll()
    }

    @Test
    fun test_getCss_bootstrap() {
        val result = apiProcessor.call {
            request {
                path = "/ui/resources/css/bootstrap"
            }
            response {
                headers = mapOf(
                        "Content-Type" to "text/css"
                )
            }
        }

        assertTrue(result.response.contentAsString.isNotBlank())
    }

    @Test
    fun test_getCss_other() {
        apiProcessor.call {
            request {
                path = "/ui/resources/css/other"
            }
            response {
                status = 404
            }
        }
    }

    @Test
    fun test_getPage_login() {
        val result = apiProcessor.call {
            request {
                path = "/ui/login?client_id=${authClient.clientKey}&redirect_uri=${authClient.getRedirectUris()[0]}&response_type=code"
            }
            response {
                headers = mapOf(
                        "Content-Type" to "text/html"
                )
            }
        }

        assertTrue(result.response.contentAsString.isNotBlank())
    }

    @Test
    fun test_getPage_noAuthCode() {
        apiProcessor.call {
            request {
                path = "/ui/login?client_id=${client2.clientKey}&redirect_uri=${authClient.getRedirectUris()[0]}&response_type=code"
            }
            response {
                status = 401
            }
        }
    }

    @Test
    fun test_getPage_other() {
        apiProcessor.call {
            request {
                path = "/ui/foo?client_id=${authClient.clientKey}&redirect_uri=${authClient.getRedirectUris()[0]}&response_type=code"
            }
            response {
                status = 404
            }
        }
    }

    @Test
    fun test_getPage_badParams() {
        apiProcessor.call {
            request {
                path = "/ui/login?client_id=${authClient.clientKey}&redirect_uri=${authClient.getRedirectUris()[0]}&response_type=code2"
            }
            response {
                status = 401
            }
        }
    }

    @Test
    fun test_getCsrf() {
        TODO("Finish this")
    }

}
