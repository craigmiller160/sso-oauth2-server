package io.craigmiller160.ssoauthserverexp.service

import io.craigmiller160.ssoauthserverexp.security.ClientAuthorities
import org.springframework.security.access.annotation.Secured
import org.springframework.stereotype.Service

@Service
class OAuth2Service {

    @Secured(ClientAuthorities.CLIENT_CREDENTIALS)
    fun clientCredentials(): String {
        return "Client Credentials"
    }

    @Secured(ClientAuthorities.PASSWORD)
    fun password(): String {
        return "Password"
    }

    @Secured(ClientAuthorities.AUTH_CODE)
    fun authCode(): String {
        return "Auth Code"
    }

}