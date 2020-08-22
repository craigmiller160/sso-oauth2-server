package io.craigmiller160.authserver.integration.oAuth2Controller

import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.apitestprocessor.body.formOf
import io.craigmiller160.apitestprocessor.config.AuthType
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import io.craigmiller160.date.converter.LegacyDateConverter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.nullValue
import org.hamcrest.text.CharSequenceLength.hasLength
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TokenClientCredentialsIntegrationTest : AbstractControllerIntegrationTest() {

    @Test
    fun `token() - client_credentials grant invalid client header`() {
        apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = formOf("grant_type" to "client_credentials")
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
    fun `token() - client_credentials grant success`() {
        val tokenResponse = apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = formOf("grant_type" to "client_credentials")
            }
        }.convert(TokenResponse::class.java)

        testTokenResponse(tokenResponse, "client_credentials")
    }

}
