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

package io.craigmiller160.authserver.integration.oAuth2Controller

import io.craigmiller160.apitestprocessor.body.Form
import io.craigmiller160.apitestprocessor.body.formOf
import io.craigmiller160.apitestprocessor.config.AuthType
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.entity.ClientUser
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import io.craigmiller160.authserver.repository.ClientUserRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.craigmiller160.authserver.security.GrantType
import io.craigmiller160.authserver.testutils.TestData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TokenPasswordIntegrationTest : AbstractControllerIntegrationTest() {

    private lateinit var otherUser: User
    private val otherUserPassword: String = "password"

    @Autowired
    private lateinit var userRepo: UserRepository

    private fun createTokenForm(
            username: String = authUser.email,
            password: String = authUserPassword
    ) = formOf(
            "grant_type" to GrantType.PASSWORD,
            "username" to username,
            "password" to password
    )

    @BeforeEach
    fun setup() {
        otherUser = TestData.createUser().copy(email = "bob@gmail.com", password = otherUserPassword)
        otherUser = userRepo.save(otherUser)
    }

    @AfterEach
    fun clean() {
        userRepo.deleteAll()
    }

    @Test
    fun `token() - password grant invalid client header`() {
        apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = createTokenForm()
                overrideAuth {
                    type = AuthType.BASIC
                    userName = "abc"
                    password = "def"
                }
            }
            response {
                status = 401
            }
        }
    }

    @Test
    fun `token() - password grant successful`() {
        val result = apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = createTokenForm()
            }
        }.convert(TokenResponse::class.java)

        testTokenResponse(result, "password", isUser = true)
    }

    @Test
    fun `token() - password grant validation`() {
        val runTest = { body: Form ->
            apiProcessor.call {
                request {
                    path = "/oauth/token"
                    method = HttpMethod.POST
                    this.body = body
                }
                response {
                    status = 400
                }
            }
        }

        runTest(createTokenForm(username = ""))
        runTest(createTokenForm(password = ""))
    }

    @Test
    fun `token() - password grant invalid credentials`() {
        val runTest = { body: Form ->
            apiProcessor.call {
                request {
                    path = "/oauth/token"
                    method = HttpMethod.POST
                    this.body = body
                }
                response {
                    status = 401
                }
            }
        }

        runTest(createTokenForm(username = "abc"))
        runTest(createTokenForm(password = "abc"))
    }

    @Test
    fun `token() - password grant user not in client`() {
        apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                this.body = createTokenForm(username = otherUser.email, password = otherUserPassword)
            }
            response {
                status = 401
            }
        }
    }

    @Test
    fun `token() - password grant with disabled client`() {
        apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                this.body = createTokenForm()
                overrideAuth {
                    type = AuthType.BASIC
                    userName = disabledClient.clientKey
                    password = validClientSecret
                }
            }
            response {
                status = 401
            }
        }
    }

    @Test
    fun `token() - password grant with disabled user`() {
        apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                this.body = createTokenForm(username = disabledUser.email)
            }
            response {
                status = 401
            }
        }
    }

}
