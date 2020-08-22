package io.craigmiller160.authserver.integration.oAuth2Controller

import io.craigmiller160.apitestprocessor.body.Form
import io.craigmiller160.apitestprocessor.body.formOf
import io.craigmiller160.apitestprocessor.config.AuthType
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.entity.RefreshToken
import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import io.craigmiller160.authserver.repository.RefreshTokenRepository
import io.craigmiller160.authserver.security.ClientUserDetails
import io.craigmiller160.authserver.security.GrantType
import io.craigmiller160.authserver.security.JwtHandler
import org.junit.jupiter.api.AfterEach
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

    @AfterEach
    fun clean() {
        refreshTokenRepo.deleteAll()
    }

    private fun createForm(refreshToken: String): Form {
        return formOf(
                "grant_type" to GrantType.REFRESH_TOKEN,
                "refresh_token" to refreshToken
        )
    }

    private fun createToken(originalGrantType: String = GrantType.CLIENT_CREDENTIALS): String {
        val clientUserDetails = ClientUserDetails(authClient)
        return jwtHandler.createRefreshToken(clientUserDetails, originalGrantType, 0, tokenId).first
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
        refreshTokenRepo.save(RefreshToken(tokenId, refreshToken, authClient.id, null, LocalDateTime.now()))

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
        TODO("Finish this")
    }

    @Test
    fun `token() - refresh_token grant without refresh_token property`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - refresh_token grant without DB token`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - refresh_token grant with bad signature`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - refresh_token grant with revoked token`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - refresh_token grant with expired token`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - refresh_token grant with bad client ID`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - refresh_token user not in client`() {
        TODO("Finish this")
    }

}
