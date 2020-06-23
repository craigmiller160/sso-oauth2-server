package io.craigmiller160.ssoauthserverexp.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OAuth2ServiceTest {

    private lateinit var oAuth2Service: OAuth2Service

    @Test
    fun test_clientCredentials() {
        val result = oAuth2Service.clientCredentials()
        assertEquals("Client Credentials", result)
    }

    @Test
    fun test_password() {
        val result = oAuth2Service.password()
        assertEquals("Password", result)
    }

    @Test
    fun test_authCode() {
        val result = oAuth2Service.authCode()
        assertEquals("Auth Code", result)
    }

}