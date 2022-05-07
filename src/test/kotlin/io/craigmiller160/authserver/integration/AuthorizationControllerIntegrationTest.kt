package io.craigmiller160.authserver.integration

import io.craigmiller160.apitestprocessor.body.Json
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class AuthorizationControllerIntegrationTest : AbstractControllerIntegrationTest() {
  @Test
  fun `Valid credentials, create and return tokens to the caller`() {
    val request = LoginTokenRequest(username = authUser.email, password = authUserPassword)
    val result =
      authApiProcessor
        .call {
          request {
            method = HttpMethod.POST
            path = "/authorization/token"
            body = Json(request)
          }
          response { status = 200 }
        }
        .convert(TokenResponse::class.java)
  }

  @Test
  fun `Valid credentials, set cookie in response to caller`() {
    TODO("Finish this")
  }

  @Test
  fun `Valid credentials, set cookie and send redirect in response to caller`() {
    TODO("Finish this")
  }

  @Test
  fun `User does not exist`() {
    TODO("Finish this")
  }

  @Test
  fun `User is disabled`() {
    TODO("Finish this")
  }

  @Test
  fun `Invalid password`() {
    TODO("Finish this")
  }
}
