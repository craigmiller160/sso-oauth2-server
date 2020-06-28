package io.craigmiller160.authserver.security

import io.craigmiller160.authserver.config.TokenConfig
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock

class AuthCodeHandlerTest {

    @Mock
    private lateinit var tokenConfig: TokenConfig

    @InjectMocks
    private lateinit var authCodeHandler: AuthCodeHandler

    @Test
    fun setup() {

    }

    @Test
    fun test_createAuthCode() {
        TODO("Finish this")
    }

    @Test
    fun test_validateAuthCode() {
        TODO("Finish this")
    }

    @Test
    fun test_validateAuthCode_expired() {
        TODO("Finish this")
    }

}
