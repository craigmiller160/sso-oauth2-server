package io.craigmiller160.ssoauthserverexp.controller

import io.craigmiller160.ssoauthserverexp.dto.TokenRequest
import io.craigmiller160.ssoauthserverexp.dto.TokenResponse
import io.craigmiller160.ssoauthserverexp.exception.BadRequestException
import io.craigmiller160.ssoauthserverexp.exception.UnsupportedGrantTypeException
import io.craigmiller160.ssoauthserverexp.security.GrantType
import io.craigmiller160.ssoauthserverexp.service.OAuth2Service
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
        val request = TokenRequest(GrantType.CLIENT_CREDENTIALS, null, null, null)
        val result = oAuth2Controller.token(request)
        assertEquals(tokenResponse, result)
    }

    @Test
    fun test_token_password() {
        val tokenResponse = TokenResponse("password", "")
        val request = TokenRequest(GrantType.PASSWORD, "user", "pass", null)
        `when`(oAuth2Service.password(request))
                .thenReturn(tokenResponse)

        val result = oAuth2Controller.token(request)
        assertEquals(tokenResponse, result)
    }

    @Test
    fun test_token_password_noUsername() {
        val request = TokenRequest(GrantType.PASSWORD, null, "pass", null)

        val ex = assertThrows<BadRequestException> { oAuth2Controller.token(request) }
        assertEquals("Invalid token request", ex.message)
    }

    @Test
    fun test_token_password_noPassword() {
        val request = TokenRequest(GrantType.PASSWORD, "user", null, null)

        val ex = assertThrows<BadRequestException> { oAuth2Controller.token(request) }
        assertEquals("Invalid token request", ex.message)
    }

    @Test
    fun test_token_authCode() {
        val tokenResponse = TokenResponse("authCode", "")
        `when`(oAuth2Service.authCode()).thenReturn(tokenResponse)
        val request = TokenRequest(GrantType.AUTH_CODE, null, null, null)
        val result = oAuth2Controller.token(request)
        assertEquals(tokenResponse, result)
    }

    @Test
    fun test_token_unsupported() {
        val request = TokenRequest("foo", null, null, null)
        val ex = assertThrows<UnsupportedGrantTypeException> { oAuth2Controller.token(request) }
        assertEquals("foo", ex.message)
    }

}