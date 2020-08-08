package io.craigmiller160.authserver.integration.oAuth2Controller

import io.craigmiller160.apitestprocessor.body.formOf
import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class AuthCodeLoginIntegrationTest : AbstractControllerIntegrationTest() {

    private val state = "STATE"

    @Test
    fun test_authCodeLogin_invalidClientHeader() {
        TODO("Finish this")
    }

    @Test
    fun test_authCodeLogin() {
        val form = formOf(
                "username" to "todo",
                "password" to "todo",
                "clientId" to validClientKey,
                "redirectUri" to authClient.redirectUri!!,
                "responseType" to "code",
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
