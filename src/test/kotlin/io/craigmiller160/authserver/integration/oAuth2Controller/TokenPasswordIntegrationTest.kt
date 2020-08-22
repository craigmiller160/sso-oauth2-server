package io.craigmiller160.authserver.integration.oAuth2Controller

import io.craigmiller160.apitestprocessor.body.formOf
import io.craigmiller160.apitestprocessor.config.AuthType
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

    @Autowired
    private lateinit var userRepo: UserRepository
    @Autowired
    private lateinit var clientUserRepo: ClientUserRepository
    private lateinit var authUser: User
    private lateinit var password: String

    private val encoder = BCryptPasswordEncoder()

    @BeforeEach
    fun setup() {
        authUser = TestData.createUser()
        password = authUser.password
        authUser = userRepo.save(authUser.copy(password = "{bcrypt}encoder.encode(password)"))

        val clientUser = ClientUser(0, authUser.id, authClient.id)
        clientUserRepo.save(clientUser)
    }

    @AfterEach
    fun clean() {
        userRepo.deleteAll()
        clientUserRepo.deleteAll()
    }


    private fun createTokenForm(
            clientId: String = validClientKey,
            username: String = authUser.email,
            password: String = this.password
    ) = formOf(
            "grant_type" to GrantType.AUTH_CODE,
            "client_id" to clientId,
            "username" to username,
            "password" to password
    )
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
        apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = createTokenForm()
            }
        }
        TODO("Finish this")
    }

    @Test
    fun `token() - password grant no username`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - password grant no password`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - password grant invalid user`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - password grant bad password`() {
        TODO("Finish this")
    }

}
