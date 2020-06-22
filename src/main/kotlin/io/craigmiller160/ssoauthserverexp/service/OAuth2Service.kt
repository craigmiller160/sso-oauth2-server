package io.craigmiller160.ssoauthserverexp.service

import org.springframework.stereotype.Service

@Service
class OAuth2Service {

    fun clientCredentials(): String {
        return "Client Credentials"
    }

    fun password(): String {
        return "Password"
    }

    fun authCode(): String {
        return "Auth Code"
    }

}