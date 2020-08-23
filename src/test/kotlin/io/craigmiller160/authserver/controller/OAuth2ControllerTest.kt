package io.craigmiller160.authserver.controller

import com.nhaarman.mockito_kotlin.verify
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.exception.BadRequestException
import io.craigmiller160.authserver.exception.UnsupportedGrantTypeException
import io.craigmiller160.authserver.security.GrantType
import io.craigmiller160.authserver.service.OAuth2Service
import io.craigmiller160.authserver.testutils.TestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import java.lang.RuntimeException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletResponse

@ExtendWith(MockitoExtension::class)
class OAuth2ControllerTest {

    @Mock
    private lateinit var oAuth2Service: OAuth2Service

    @InjectMocks
    private lateinit var oAuth2Controller: OAuth2Controller

    @Mock
    private lateinit var res: HttpServletResponse

    private val authCode = "authCode"

    @Test
    @Disabled
    fun test_token_clientCredentials() {
        val tokenResponse = TokenResponse("clientCredentials", "", "")
        `when`(oAuth2Service.clientCredentials()).thenReturn(tokenResponse)
        val request = TestData.createTokenRequest(GrantType.CLIENT_CREDENTIALS)
        val result = oAuth2Controller.token(request)
        assertEquals(tokenResponse, result)
    }

    @Test
    fun test_authCodeLogin() {
        val login = TestData.createAuthCodeLogin()
        `when`(oAuth2Service.authCodeLogin(login))
                .thenReturn(authCode)

        val headerNameCaptor = ArgumentCaptor.forClass(String::class.java)
        val headerValueCaptor = ArgumentCaptor.forClass(String::class.java)
        val statusCaptor = ArgumentCaptor.forClass(Int::class.java)

        oAuth2Controller.authCodeLogin(login, res)

        verify(res, times(1))
                .status = statusCaptor.capture()
        verify(res, times(1))
                .addHeader(headerNameCaptor.capture(), headerValueCaptor.capture())

        assertEquals(302, statusCaptor.value)
        assertEquals("Location", headerNameCaptor.value)
        assertEquals("${login.redirectUri}?code=$authCode&state=${login.state}", headerValueCaptor.value)
    }

    @Test
    fun test_authCodeLogin_fail() {
        val login = TestData.createAuthCodeLogin()
        `when`(oAuth2Service.authCodeLogin(login))
                .thenThrow(RuntimeException("Failing"))

        val headerNameCaptor = ArgumentCaptor.forClass(String::class.java)
        val headerValueCaptor = ArgumentCaptor.forClass(String::class.java)
        val statusCaptor = ArgumentCaptor.forClass(Int::class.java)

        oAuth2Controller.authCodeLogin(login, res)

        verify(res, times(1))
                .status = statusCaptor.capture()
        verify(res, times(1))
                .addHeader(headerNameCaptor.capture(), headerValueCaptor.capture())

        assertEquals(302, statusCaptor.value)
        assertEquals("Location", headerNameCaptor.value)

        val redirect = URLEncoder.encode(login.redirectUri, StandardCharsets.UTF_8)
        val expectedUri = "/ui/login.html?response_type=${login.responseType}&client_id=${login.clientId}&redirect_uri=$redirect&state=${login.state}&fail=true"
        assertEquals(expectedUri, headerValueCaptor.value)
    }

    @Test
    fun test_token_password() {
        val tokenResponse = TokenResponse("password", "", "")
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
        val request = TestData.createTokenRequest(GrantType.AUTH_CODE, clientId = "key", code = "code", redirectUri = "uri")
        val tokenResponse = TokenResponse("authCode", "", "")
        `when`(oAuth2Service.authCode(request))
                .thenReturn(tokenResponse)
        val result = oAuth2Controller.token(request)
        assertEquals(tokenResponse, result)
    }

    @Test
    fun test_token_authCode_noClientId() {
        val request = TestData.createTokenRequest(GrantType.AUTH_CODE, code = "code", redirectUri = "uri")

        val ex = assertThrows<BadRequestException> { oAuth2Controller.token(request) }
        assertEquals("Invalid token request", ex.message)
    }

    @Test
    fun test_token_authCode_noCode() {
        val request = TestData.createTokenRequest(GrantType.AUTH_CODE, clientId = "key", redirectUri = "uri")

        val ex = assertThrows<BadRequestException> { oAuth2Controller.token(request) }
        assertEquals("Invalid token request", ex.message)
    }

    @Test
    fun test_token_authCode_noRedirectUri() {
        val request = TestData.createTokenRequest(GrantType.AUTH_CODE, clientId = "key", code = "code")

        val ex = assertThrows<BadRequestException> { oAuth2Controller.token(request) }
        assertEquals("Invalid token request", ex.message)
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
        val tokenResponse = TokenResponse("refresh", "", "")
        val token = "ABCDEFG"
        val request = TestData.createTokenRequest(GrantType.REFRESH_TOKEN, refreshToken = token)
        `when`(oAuth2Service.refresh(token))
                .thenReturn(tokenResponse)

        val result = oAuth2Controller.token(request)
        assertEquals(tokenResponse, result)
    }

}
