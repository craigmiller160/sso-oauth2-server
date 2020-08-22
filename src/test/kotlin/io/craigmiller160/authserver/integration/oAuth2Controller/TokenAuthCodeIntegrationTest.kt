package io.craigmiller160.authserver.integration.oAuth2Controller

import io.craigmiller160.apitestprocessor.body.Form
import io.craigmiller160.apitestprocessor.body.formOf
import io.craigmiller160.apitestprocessor.config.AuthType
import io.craigmiller160.authserver.config.TokenConfig
import io.craigmiller160.authserver.entity.ClientUser
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import io.craigmiller160.authserver.repository.ClientUserRepository
import io.craigmiller160.authserver.repository.UserRepository
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
import java.util.Base64
import javax.crypto.Cipher

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TokenAuthCodeIntegrationTest : AbstractControllerIntegrationTest() {

    @Autowired
    private lateinit var authCodeHandler: AuthCodeHandler

    @Autowired
    private lateinit var userRepo: UserRepository
    @Autowired
    private lateinit var clientUserRepo: ClientUserRepository
    private lateinit var authUser: User

    @BeforeEach
    fun setup() {
        authUser = TestData.createUser()
        authUser = userRepo.save(authUser)

        val clientUser = ClientUser(0, authUser.id, authClient.id)
        clientUserRepo.save(clientUser)
    }

    @AfterEach
    fun clean() {
        userRepo.deleteAll()
        clientUserRepo.deleteAll()
    }

    private fun createTokenForm() = formOf(
            "grant_type" to GrantType.AUTH_CODE,
            "client_id" to validClientKey,
            "code" to authCodeHandler.createAuthCode(authClient.id, authUser.id, 1000000),
            "redirect_uri" to authClient.redirectUri!!
    )

    @Test
    fun `token() - auth_code grant invalid client header`() {
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
    fun `test() - auth_code grant success`() {
        apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = createTokenForm()
            }
        }
    }

    @Test
    fun `test() - auth_code grant validation`() {
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

        runTest(createTokenForm())
        TODO("Finish this")
    }

    @Test
    fun `test() - auth_code grant without client_id`() {
        TODO("Finish this")
    }

    @Test
    fun `test() - auth_code grant without code`() {
        TODO("Finish this")
    }

    @Test
    fun `test() - auth_code grant without redirect_uri`() {
        TODO("Finish this")
    }

    @Test
    fun `test() - auth_code grant with wrong client key`() {
        TODO("Finish this")
    }

    @Test
    fun `test() - auth_code grant with wrong redirect uri`() {
        TODO("Finish this")
    }

    @Test
    fun `test() - auth_code grant with invalid auth code`() {
        TODO("Finish this")
    }

    @Test
    fun `test() - auth_code grant with expired auth code`() {
        TODO("Finish this")
    }

}
