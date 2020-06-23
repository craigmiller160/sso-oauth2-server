package io.craigmiller160.ssoauthserverexp.service

import io.craigmiller160.ssoauthserverexp.repository.RefreshTokenRepository
import io.craigmiller160.ssoauthserverexp.security.ClientAuthorities
import io.craigmiller160.ssoauthserverexp.security.JwtCreator
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class OAuth2Service (
        private val jwtCreator: JwtCreator,
        private val refreshTokenRepo: RefreshTokenRepository
) {

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