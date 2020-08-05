package io.craigmiller160.authserver.integration.oAuth2Controller

import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class AuthCodeLoginIntegrationTest : AbstractControllerIntegrationTest() {

    @Test
    fun test_authCodeLogin() {
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
