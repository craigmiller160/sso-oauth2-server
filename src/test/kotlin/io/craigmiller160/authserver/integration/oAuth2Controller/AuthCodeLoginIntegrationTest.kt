package io.craigmiller160.authserver.integration.oAuth2Controller

import io.craigmiller160.apitestprocessor.body.formOf
import io.craigmiller160.authserver.entity.ClientUser
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import io.craigmiller160.authserver.repository.ClientUserRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.craigmiller160.authserver.testutils.TestData
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
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
class AuthCodeLoginIntegrationTest : AbstractControllerIntegrationTest() {

    private val state = "STATE"
    private val responseType = "code"
    private val encoder = BCryptPasswordEncoder()

    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var clientUserRepo: ClientUserRepository

    private lateinit var user: User
    private lateinit var password: String

    @BeforeEach
    fun setup() {
        user = TestData.createUser()
        password = user.password
        user = user.copy(password = "{bcrypt}${encoder.encode(user.password)}")
        user = userRepo.save(user)

        val clientUser = ClientUser(0, authClient.id, user.id)
        clientUserRepo.save(clientUser)
    }

    @AfterEach
    fun clean() {
        clientUserRepo.deleteAll()
        userRepo.deleteAll()
    }

    @Test
    fun test_authCodeLogin_invalidClientHeader() {
        TODO("Finish this")
    }

    @Test
    fun test_authCodeLogin() {
        val form = formOf(
                "username" to user.email,
                "password" to password,
                "clientId" to validClientKey,
                "redirectUri" to authClient.redirectUri!!,
                "responseType" to responseType,
                "state" to state
        )

        val result = apiProcessor.call {
            request {
                path = "/oauth/auth"
                method = HttpMethod.POST
                body = form
            }
            response {
                status = 302
            }
        }
        val location = result.response.getHeader("Location")
        assertNotNull(location)
        val uri = location!!.split("?")[0]
        val query = location.split("?")[1]
                .split("&")
                .map { keyValue ->
                    val parts = keyValue.split("=")
                    Pair(parts[0], parts[1])
                }
                .toMap()
        assertThat(uri, equalTo(authClient.redirectUri))
        assertThat(query["code"], notNullValue())
        assertThat(query["state"], equalTo(state))
    }

    @Test
    fun test_authCodeLogin_invalidLogin() {
        TODO("Finish this")
    }

    @Test
    fun test_authCodeLogin_missingState() {
        TODO("Finish this")
    }

    @Test
    fun test_authCodeLogin_badResponseType() {
        TODO("Finish this")
    }

    @Test
    fun test_authCodeLogin_noClient() {
        TODO("Finish this")
    }

    @Test
    fun test_authCodeLogin_noAuthCode() {
        TODO("Finish this")
    }

    @Test
    fun test_authCodeLogin_noUser() {
        TODO("Finish this")
    }

    @Test
    fun test_authCodeLogin_badCreds() {
        TODO("Finish this")
    }

}
