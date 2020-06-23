package io.craigmiller160.ssoauthserverexp.security

import io.craigmiller160.ssoauthserverexp.config.TokenConfig
import io.craigmiller160.ssoauthserverexp.util.LegacyDateConverter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class JwtCreatorTest {

    @Mock
    private lateinit var tokenConfig: TokenConfig
    @Mock
    private lateinit var legacyDateConverter: LegacyDateConverter

    @InjectMocks
    private lateinit var jwtCreator: JwtCreator

    @Test
    fun test_createAccessToken() {
        TODO("Finish this")
    }

    @Test
    fun test_createRefreshToken() {
        TODO("Finish this")
    }

}