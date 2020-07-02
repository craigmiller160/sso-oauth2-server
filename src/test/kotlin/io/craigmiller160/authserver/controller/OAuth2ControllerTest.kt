package io.craigmiller160.authserver.controller

import io.craigmiller160.authserver.dto.TokenRequest
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.exception.BadRequestException
import io.craigmiller160.authserver.exception.UnsupportedGrantTypeException
import io.craigmiller160.authserver.security.GrantType
import io.craigmiller160.authserver.service.OAuth2Service
import io.craigmiller160.authserver.testutils.TestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class OAuth2ControllerTest {

    @Mock
    private lateinit var oAuth2Service: OAuth2Service

    @InjectMocks
    private lateinit var oAuth2Controller: OAuth2Controller

    @Test
    fun test_token_clientCredentials() {
        val tokenResponse = TokenResponse("clientCredentials", "")
        `when`(oAuth2Service.clientCredentials()).thenReturn(tokenResponse)
        val request = TestData.createTokenRequest(GrantType.CLIENT_CREDENTIALS)
        val result = oAuth2Controller.token(request)
        assertEquals(tokenResponse, result)
    }

    @Test
    fun test_authCode() {
        TODO("Finish this")
    }

    @Test
    fun test_token_password() {
        val tokenResponse = TokenResponse("password", "")
        val request = TestData.createTokenRequest(GrantType.PASSWORD, username = "user", password = "pass")
        `when`(oAuth2Service.password(request))
                .thenReturn(tokenResponse)

        val result = oAuth2Controller.token(request)
        assertEquals(tokenResponse, result)
    }

    @Test
    fun test_token_password_noUsername() {
        val request = TestData.createTokenRequest(GrantType.PASSWORD, username = null, password = "pass")

        val ex = assertThrows<BadRequestException> { oAuth2Controller.token(request) }
        assertEquals("Invalid token request", ex.message)
    }

    @Test
    fun test_token_password_noPassword() {
        val request = TestData.createTokenRequest(GrantType.PASSWORD, username = "user")

        val ex = assertThrows<BadRequestException> { oAuth2Controller.token(request) }
        assertEquals("Invalid token request", ex.message)
    }

    @Test
    fun test_token_authCode() {
        val tokenResponse = TokenResponse("authCode", "")
        `when`(oAuth2Service.authCode(TestData.createTokenRequest(GrantType.AUTH_CODE))).thenReturn(tokenResponse)
        val request = TestData.createTokenRequest(GrantType.AUTH_CODE)
        val result = oAuth2Controller.token(request)
        assertEquals(tokenResponse, result)
    }

    @Test
    fun test_token_unsupported() {
        val request = TestData.createTokenRequest("foo")
        val ex = assertThrows<UnsupportedGrantTypeException> { oAuth2Controller.token(request) }
        assertEquals("foo", ex.message)
    }

    @Test
    fun test_token_refresh_noToken() {
        val request = TestData.createTokenRequest(GrantType.REFRESH_TOKEN)
        val ex = assertThrows<BadRequestException> { oAuth2Controller.token(request) }
        assertEquals("Invalid token request", ex.message)
    }

    @Test
    fun test_token_refresh() {
        val tokenResponse = TokenResponse("refresh", "")
        val token = "ABCDEFG"
        val request = TestData.createTokenRequest(GrantType.REFRESH_TOKEN, refreshToken = token)
        `when`(oAuth2Service.refresh(token))
                .thenReturn(tokenResponse)

        val result = oAuth2Controller.token(request)
        assertEquals(tokenResponse, result)
    }

}
