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

package io.craigmiller160.authserver.integration.oAuth2Controller

import io.craigmiller160.apitestprocessor.body.Form
import io.craigmiller160.apitestprocessor.body.formOf
import io.craigmiller160.apitestprocessor.config.AuthType
import io.craigmiller160.authserver.dto.tokenResponse.TokenResponse
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import io.craigmiller160.authserver.security.AuthCodeHandler
import io.craigmiller160.authserver.security.GrantType
import io.craigmiller160.authserver.testutils.TestData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TokenAuthCodeIntegrationTest : AbstractControllerIntegrationTest() {

  private lateinit var otherUser: User
  private val otherUserPassword: String = "password"

  @Autowired private lateinit var authCodeHandler: AuthCodeHandler

  private fun createTokenForm(
      clientId: String = validClientKey,
      redirectUri: String = authClient.getRedirectUris()[0],
      code: String = authCodeHandler.createAuthCode(authClient.id, authUser.id, 1000000)
  ) =
      formOf(
          "grant_type" to GrantType.AUTH_CODE,
          "client_id" to clientId,
          "code" to code,
          "redirect_uri" to redirectUri)

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
  fun `token() - auth_code grant invalid client header`() {
    oauth2ApiProcessor.call {
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
      response { status = 401 }
    }
  }

  @Test
  fun `token() - auth_code grant success`() {
    val result =
        oauth2ApiProcessor
            .call {
              request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = createTokenForm()
              }
            }
            .convert(TokenResponse::class.java)

    testTokenResponse(result, "authorization_code", isUser = true)
  }

  @Test
  fun `token() - auth_code grant validation rules`() {
    val runTest = { body: Form ->
      oauth2ApiProcessor.call {
        request {
          path = "/oauth/token"
          method = HttpMethod.POST
          this.body = body
        }
        response { status = 400 }
      }
    }

    runTest(createTokenForm(clientId = ""))
    runTest(createTokenForm(code = ""))
    runTest(createTokenForm(redirectUri = ""))
  }

  @Test
  fun `token() - auth_code grant with invalid login`() {
    val runTest = { body: Form ->
      oauth2ApiProcessor.call {
        request {
          path = "/oauth/token"
          method = HttpMethod.POST
          this.body = body
        }
        response { status = 401 }
      }
    }

    runTest(createTokenForm(clientId = "abc"))
    runTest(createTokenForm(redirectUri = "abc"))
    runTest(createTokenForm(code = "abc"))
    runTest(
        createTokenForm(code = authCodeHandler.createAuthCode(authClient.id, authUser.id, -1000)))
  }

  @Test
  fun `token() - auth_code grant user not in client`() {
    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        this.body =
            createTokenForm(
                code = authCodeHandler.createAuthCode(authClient.id, otherUser.id, 10000))
      }
      response { status = 401 }
    }
  }

  @Test
  fun `token() - auth_code grant with disabled client`() {
    val form =
        createTokenForm(
            clientId = disabledClient.clientKey,
            code = authCodeHandler.createAuthCode(disabledClient.id, authUser.id, 1000000))

    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = form
        overrideAuth {
          type = AuthType.BASIC
          userName = disabledClient.clientKey
          password = validClientSecret
        }
      }
      response { status = 401 }
    }
  }

  @Test
  fun `token() - auth_code grant with disabled user`() {
    val form =
        createTokenForm(
            code = authCodeHandler.createAuthCode(authClient.id, disabledUser.id, 100000))

    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = form
      }
      response { status = 401 }
    }
  }
}
