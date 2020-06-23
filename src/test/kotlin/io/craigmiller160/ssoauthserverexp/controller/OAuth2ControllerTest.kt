//package io.craigmiller160.ssoauthserverexp.controller
//
//import io.craigmiller160.ssoauthserverexp.dto.TokenRequest
//import io.craigmiller160.ssoauthserverexp.exception.UnsupportedGrantTypeException
//import io.craigmiller160.ssoauthserverexp.security.GrantTypes
//import io.craigmiller160.ssoauthserverexp.service.OAuth2Service
//import org.junit.Assert.assertEquals
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.InjectMocks
//import org.mockito.Mock
//import org.mockito.Mockito.`when`
//import org.mockito.junit.MockitoJUnitRunner
//
//@RunWith(MockitoJUnitRunner::class)
//class OAuth2ControllerTest {
//
//    @Mock
//    private lateinit var oAuth2Service: OAuth2Service
//
//    @InjectMocks
//    private lateinit var oAuth2Controller: OAuth2Controller
//
//    @Test
//    fun test_token_clientCredentials() {
//        `when`(oAuth2Service.clientCredentials()).thenReturn("Success")
//        val request = TokenRequest(grant_type = GrantTypes.CLIENT_CREDENTIALS)
//        val result = oAuth2Controller.token(request)
//        assertEquals("Success", result)
//    }
//
//    @Test
//    fun test_token_password() {
//        `when`(oAuth2Service.password()).thenReturn("Success")
//        val request = TokenRequest(grant_type = GrantTypes.PASSWORD)
//        val result = oAuth2Controller.token(request)
//        assertEquals("Success", result)
//    }
//
//    @Test
//    fun test_token_authCode() {
//        `when`(oAuth2Service.authCode()).thenReturn("Success")
//        val request = TokenRequest(grant_type = GrantTypes.AUTH_CODE)
//        val result = oAuth2Controller.token(request)
//        assertEquals("Success", result)
//    }
//
//    @Test(expected = UnsupportedGrantTypeException::class)
//    fun test_token_unsupported() {
//        val request = TokenRequest(grant_type = "foo")
//        oAuth2Controller.token(request)
//    }
//
//}