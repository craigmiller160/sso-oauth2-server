package io.craigmiller160.authserver.integration.oAuth2Controller

import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import io.craigmiller160.authserver.repository.ClientRepository
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
class TokenClientCredentialsIntegrationTest : AbstractControllerIntegrationTest() {

    @Autowired
    private lateinit var clientRepo: ClientRepository

    private lateinit var client1: Client

    @BeforeEach
    fun setup() {
        client1 = TestData.createClient()
        client1 = clientRepo.save(client1)
    }

    @AfterEach
    fun clean() {
        clientRepo.deleteAll()
    }

    @Test
    fun `token() - client_credentials grant invalid client header`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - client_credentials grant success`() {
        val tokenResponse = apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
            }
        }.convert(TokenResponse::class.java)

        println(tokenResponse) // TODO delete this
    }

}
