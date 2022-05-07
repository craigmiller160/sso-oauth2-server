package io.craigmiller160.authserver.integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class AuthorizationControllerIntegrationTest : AbstractControllerIntegrationTest() {
  @Test
  fun token() {
    TODO("Finish this")
  }
}
