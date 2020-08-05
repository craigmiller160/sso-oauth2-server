package io.craigmiller160.authserver.integration.oAuth2Controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TokenClientCredentialsIntegrationTest {

    @Test
    fun `token() - client_credentials grant invalid client header`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - client_credentials grant success`() {
        TODO("Finish this")
    }

}
