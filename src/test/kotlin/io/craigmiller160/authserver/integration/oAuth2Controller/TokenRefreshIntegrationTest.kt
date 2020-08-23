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
import io.craigmiller160.authserver.security.ClientUserDetails
import io.craigmiller160.authserver.security.GrantType
import io.craigmiller160.authserver.security.JwtHandler
import io.craigmiller160.authserver.testutils.TestData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TokenRefreshIntegrationTest : AbstractControllerIntegrationTest() {

    private val tokenId = "12345"

    @Autowired
    private lateinit var jwtHandler: JwtHandler

    @Autowired
    private lateinit var refreshTokenRepo: RefreshTokenRepository

    private lateinit var otherUser: User
    private val otherUserPassword: String = "password"

    @Autowired
    private lateinit var userRepo: UserRepository

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
        return formOf(
                "grant_type" to GrantType.REFRESH_TOKEN,
                "refresh_token" to refreshToken
        )
    }

    private fun createToken(originalGrantType: String = GrantType.CLIENT_CREDENTIALS, client: Client = authClient, userId: Long = 0): String {
        val clientUserDetails = ClientUserDetails(client)
        val refreshToken =  jwtHandler.createRefreshToken(clientUserDetails, originalGrantType, userId, tokenId).first
        refreshTokenRepo.save(RefreshToken(tokenId, refreshToken, client.id, userId, LocalDateTime.now()))
        return refreshToken
    }

    @Test
    fun `token() - refresh_token grant invalid client header`() {
        apiProcessor.call {
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
            response {
                status = 401
            }
        }
    }

    @Test
    fun `token() - successful refresh_token grant for client only`() {
        val refreshToken = createToken()

        val result = apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = createForm(refreshToken)
            }
        }.convert(TokenResponse::class.java)

        testTokenResponse(result, GrantType.CLIENT_CREDENTIALS)
    }

    @Test
    fun `token() - successful refresh_token grant with user`() {
        val refreshToken = createToken(GrantType.PASSWORD, userId = authUser.id)

        val result = apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = createForm(refreshToken)
            }
        }.convert(TokenResponse::class.java)

        testTokenResponse(result, GrantType.PASSWORD, isUser = true)
    }

    @Test
    fun `token() - refresh_token grant validations`() {
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

        runTest(createForm(""))
    }

    @Test
    fun `token() - refresh_token grant with bad signature`() {
        val initRefreshToken = createToken()
        val tokenParts = initRefreshToken.split(".")
        val refreshToken = "${tokenParts[0]}.${tokenParts[1]}.ABCDEFG"

        apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                this.body = createForm(refreshToken)
            }
            response {
                status = 401
            }
        }
    }

    @Test
    fun `token() - refresh_token grant with revoked token`() {
        val refreshToken = createToken()
        refreshTokenRepo.deleteAll()

        apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = createForm(refreshToken)
            }
            response {
                status = 401
            }
        }
    }

    @Test
    fun `token() - refresh_token grant with expired token`() {
        val client = authClient.copy(refreshTokenTimeoutSecs = -1000)
        val refreshToken = createToken(client = client)

        apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = createForm(refreshToken)
            }
            response {
                status = 401
            }
        }
    }

    @Test
    fun `token() - refresh_token grant with bad client ID`() {
        val refreshToken = createToken(client = authClient.copy(id = 10000))

        apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = createForm(refreshToken)
            }
            response {
                status = 401
            }
        }
    }

    @Test
    fun `token() - refresh_token grant user not in client`() {
        val refreshToken = createToken(userId = otherUser.id)

        apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = createForm(refreshToken)
            }
            response {
                status = 401
            }
        }
    }

    @Test
    fun `token() - refresh_token grant with disabled client`() {
        val refreshToken = createToken(client = disabledClient)

        apiProcessor.call {
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
            response {
                status = 401
            }
        }
    }

}
