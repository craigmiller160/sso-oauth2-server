package io.craigmiller160.ssoauthserverexp.service

import com.nhaarman.mockito_kotlin.isA
import io.craigmiller160.ssoauthserverexp.dto.TokenResponse
import io.craigmiller160.ssoauthserverexp.entity.RefreshToken
import io.craigmiller160.ssoauthserverexp.repository.RefreshTokenRepository
import io.craigmiller160.ssoauthserverexp.security.JwtCreator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class OAuth2ServiceTest {

    @Mock
    private lateinit var jwtCreator: JwtCreator
    @Mock
    private lateinit var refreshTokenRepo: RefreshTokenRepository

    @InjectMocks
    private lateinit var oAuth2Service: OAuth2Service

    private val accessToken = "AccessToken"
    private val refreshToken = "RefreshToken"

    @Test
    fun test_clientCredentials() {
//        `when`(jwtCreator.createAccessToken())
//                .thenReturn(accessToken)
        `when`(jwtCreator.createRefreshToken())
                .thenReturn(refreshToken)

        val result = oAuth2Service.clientCredentials()
        assertEquals(TokenResponse(accessToken, refreshToken), result)

        verify(refreshTokenRepo, times(1))
                .save(isA<RefreshToken>())
        TODO("Finish this")
    }

    @Test
    fun test_password() {
//        val result = oAuth2Service.password()
//        assertEquals(TokenResponse("password", ""), result)
        TODO("Finish this")
    }

    @Test
    fun test_authCode() {
        val result = oAuth2Service.authCode()
        assertEquals(TokenResponse("authCode", ""), result)
    }

}