package io.craigmiller160.authserver.integration.oAuth2Controller

import io.craigmiller160.apitestprocessor.body.formOf
import io.craigmiller160.apitestprocessor.config.AuthType
import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import io.craigmiller160.authserver.security.GrantType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TokenAuthCodeIntegrationTest : AbstractControllerIntegrationTest() {

    private fun createTokenForm() = formOf(
            "grant_type" to GrantType.AUTH_CODE,
            "client_id" to validClientKey,
            "code" to "todo",
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
