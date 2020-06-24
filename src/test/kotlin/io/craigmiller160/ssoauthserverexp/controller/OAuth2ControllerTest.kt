package io.craigmiller160.ssoauthserverexp.controller

import io.craigmiller160.ssoauthserverexp.dto.TokenRequest
import io.craigmiller160.ssoauthserverexp.dto.TokenResponse
import io.craigmiller160.ssoauthserverexp.exception.UnsupportedGrantTypeException
import io.craigmiller160.ssoauthserverexp.security.GrantTypes
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
        val request = TokenRequest(GrantTypes.CLIENT_CREDENTIALS, null, null, null)
        val result = oAuth2Controller.token(request)
        assertEquals(tokenResponse, result)
    }

    @Test
    fun test_token_password() {
//        val tokenResponse = TokenResponse("password", "")
//        `when`(oAuth2Service.password()).thenReturn(tokenResponse)
//        val request = TokenRequest(grant_type = GrantTypes.PASSWORD)
//        val result = oAuth2Controller.token(request)
//        assertEquals(tokenResponse, result)
        TODO("Finish this")
    }

    @Test
    fun test_token_authCode() {
        val tokenResponse = TokenResponse("authCode", "")
        `when`(oAuth2Service.authCode()).thenReturn(tokenResponse)
        val request = TokenRequest(GrantTypes.AUTH_CODE, null, null, null)
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