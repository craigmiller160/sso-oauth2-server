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

import io.craigmiller160.apitestprocessor.body.formOf
import io.craigmiller160.apitestprocessor.config.AuthType
import io.craigmiller160.authserver.dto.tokenResponse.TokenResponse
import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import io.craigmiller160.authserver.security.GrantType
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TokenClientCredentialsIntegrationTest : AbstractControllerIntegrationTest() {

  @Test
  fun `token() - client_credentials grant invalid client header`() {
    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = formOf("grant_type" to "client_credentials")
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
  fun `token() - client_credentials not allowed`() {
    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = formOf("grant_type" to GrantType.CLIENT_CREDENTIALS)
      }
      response { status = 400 }
    }
  }

  @Test
  @Disabled
  fun `token() - client_credentials grant success`() {
    val tokenResponse =
        oauth2ApiProcessor
            .call {
              request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = formOf("grant_type" to GrantType.CLIENT_CREDENTIALS)
              }
            }
            .convert(TokenResponse::class.java)

    testTokenResponse(tokenResponse, "client_credentials")
  }

  @Test
  fun `token() - client_credentials grant with disabled client`() {
    oauth2ApiProcessor.call {
      request {
        path = "/oauth/token"
        method = HttpMethod.POST
        body = formOf("grant_type" to "client_credentials")
        overrideAuth {
          type = AuthType.BASIC
          userName = disabledClient.clientKey
          password = validClientSecret
        }
      }
      response { status = 401 }
    }
  }
}
