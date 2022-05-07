package io.craigmiller160.authserver.integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class AuthorizationControllerIntegrationTest : AbstractControllerIntegrationTest() {
  @Test
  fun `Valid credentials, create and return tokens to the caller`() {
    TODO("Finish this")
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
  fun `Invalid password`() {
    TODO("Finish this")
  }
}
