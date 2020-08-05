package io.craigmiller160.authserver.integration.oAuth2Controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TokenAuthCodeIntegrationTest {

    @Test
    fun `token() - auth_code grant invalid client header`() {
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

    @Test
    fun `test() - auth_code grant success`() {
        TODO("Finish this")
    }

}
