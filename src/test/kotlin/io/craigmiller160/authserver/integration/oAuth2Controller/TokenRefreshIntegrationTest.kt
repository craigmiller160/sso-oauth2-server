package io.craigmiller160.authserver.integration.oAuth2Controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TokenRefreshIntegrationTest {

    @Test
    fun `token() - refresh_token grant invalid client header`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - successful refresh_token grant`() {
        TODO("Finish this")
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
