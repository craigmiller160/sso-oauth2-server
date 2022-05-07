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
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.RefreshToken
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import io.craigmiller160.authserver.repository.RefreshTokenRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.craigmiller160.authserver.security.GrantType
import io.craigmiller160.authserver.security.OAuth2ClientUserDetails
import io.craigmiller160.authserver.security.OAuth2JwtHandler
import io.craigmiller160.authserver.testutils.TestData
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TokenRefreshIntegrationTest : AbstractControllerIntegrationTest() {

  private val tokenId = "12345"

  @Autowired private lateinit var OAuth2JwtHandler: OAuth2JwtHandler

  @Autowired private lateinit var refreshTokenRepo: RefreshTokenRepository

  private lateinit var otherUser: User
  private val otherUserPassword: String = "password"

  @Autowired private lateinit var userRepo: UserRepository

  @BeforeEach
  fun setup() {
    otherUser = TestData.createUser().copy(email = "bob@gmail.com", password = otherUserPassword)
    otherUser = userRepo.save(otherUser)
  }

  @AfterEach
  fun clean() {
    refreshTokenRepo.deleteAll()
    userRepo.deleteAll()
  }

  private fun createForm(refreshToken: String): Form {
    return formOf("grant_type" to GrantType.REFRESH_TOKEN, "refresh_token" to refreshToken)
  }

  private fun createToken(
    originalGrantType: String = GrantType.CLIENT_CREDENTIALS,
    client: Client = authClient,
    userId: Long = 0
  ): String {
    val OAuth2ClientUserDetails = OAuth2ClientUserDetails(client)
    val refreshToken =
      OAuth2JwtHandler.createRefreshToken(
          OAuth2ClientUserDetails, originalGrantType, userId, tokenId)
        .first
    refreshTokenRepo.save(
      RefreshToken(tokenId, refreshToken, client.id, userId, ZonedDateTime.now(ZoneId.of("UTC"))))
    return refreshToken
  }

  @Test
  fun `token() - refresh_token grant invalid client header`() {
    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = createForm("")
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
  @Disabled
  fun `token() - successful refresh_token grant for client only`() {
    val refreshToken = createToken()

    val result =
      oauth2ApiProcessor
        .call {
          request {
            path = "/oauth/token"
            method = HttpMethod.POST
            body = createForm(refreshToken)
          }
        }
        .convert(TokenResponse::class.java)

    testTokenResponse(result, GrantType.CLIENT_CREDENTIALS)
  }

  @Test
  @Disabled
  fun `token() - called multiple times with the same refresh token, client only`() {
    val refreshToken = createToken()

    val result1 =
      oauth2ApiProcessor
        .call {
          request {
            path = "/oauth/token"
            method = HttpMethod.POST
            body = createForm(refreshToken)
          }
        }
        .convert(TokenResponse::class.java)

    val result2 =
      oauth2ApiProcessor
        .call {
          request {
            path = "/oauth/token"
            method = HttpMethod.POST
            body = createForm(refreshToken)
          }
        }
        .convert(TokenResponse::class.java)

    testTokenResponse(result1, GrantType.CLIENT_CREDENTIALS)
    testTokenResponse(result2, GrantType.CLIENT_CREDENTIALS)
  }

  @Test
  fun `token() - refresh_token grant for client only not allowed`() {
    val refreshToken = createToken()

    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = createForm(refreshToken)
      }
      response { status = 400 }
    }
  }

  @Test
  fun `token() - successful refresh_token grant with user`() {
    val refreshToken = createToken(GrantType.PASSWORD, userId = authUser.id)

    val result =
      oauth2ApiProcessor
        .call {
          request {
            path = "/oauth/token"
            method = HttpMethod.POST
            body = createForm(refreshToken)
          }
        }
        .convert(TokenResponse::class.java)

    testTokenResponse(result, GrantType.PASSWORD, isUser = true)
  }

  @Test
  fun `token() - called multiple times with the same refresh token, with user`() {
    val refreshToken = createToken(GrantType.PASSWORD, userId = authUser.id)

    val result1 =
      oauth2ApiProcessor
        .call {
          request {
            path = "/oauth/token"
            method = HttpMethod.POST
            body = createForm(refreshToken)
          }
        }
        .convert(TokenResponse::class.java)

    val result2 =
      oauth2ApiProcessor
        .call {
          request {
            path = "/oauth/token"
            method = HttpMethod.POST
            body = createForm(refreshToken)
          }
        }
        .convert(TokenResponse::class.java)

    testTokenResponse(result1, GrantType.PASSWORD, isUser = true)
    testTokenResponse(result2, GrantType.PASSWORD, isUser = true)

    assertEquals(1, refreshTokenRepo.count())
  }

  @Test
  fun `token() - refresh_token grant validations`() {
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

    runTest(createForm(""))
  }

  @Test
  fun `token() - refresh_token grant with bad signature`() {
    val initRefreshToken = createToken()
    val tokenParts = initRefreshToken.split(".")
    val refreshToken = "${tokenParts[0]}.${tokenParts[1]}.ABCDEFG"

    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        this.body = createForm(refreshToken)
      }
      response { status = 401 }
    }
  }

  @Test
  fun `token() - refresh_token grant with revoked token`() {
    val refreshToken = createToken(originalGrantType = GrantType.PASSWORD, userId = authUser.id)
    refreshTokenRepo.deleteAll()

    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = createForm(refreshToken)
      }
      response { status = 401 }
    }
  }

  @Test
  fun `token() - refresh_token grant with expired token`() {
    val client = authClient.copy(refreshTokenTimeoutSecs = -1000)
    val refreshToken = createToken(client = client)

    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = createForm(refreshToken)
      }
      response { status = 401 }
    }
  }

  @Test
  fun `token() - refresh_token grant with bad client ID`() {
    val refreshToken = createToken(client = authClient.copy(id = 10000))

    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = createForm(refreshToken)
      }
      response { status = 401 }
    }
  }

  @Test
  fun `token() - refresh_token grant user not in client`() {
    val refreshToken = createToken(originalGrantType = GrantType.PASSWORD, userId = otherUser.id)

    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = createForm(refreshToken)
      }
      response { status = 401 }
    }
  }

  @Test
  fun `token() - refresh_token grant with disabled client`() {
    val refreshToken = createToken(client = disabledClient)

    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = createForm(refreshToken)
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
  fun `token() - refresh_token grant with disabled user`() {
    val refreshToken = createToken(originalGrantType = GrantType.PASSWORD, userId = disabledUser.id)

    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = createForm(refreshToken)
      }
      response { status = 401 }
    }
  }
}
