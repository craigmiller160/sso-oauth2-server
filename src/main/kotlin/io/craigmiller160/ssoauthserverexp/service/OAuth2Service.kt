package io.craigmiller160.ssoauthserverexp.service

import io.craigmiller160.ssoauthserverexp.dto.TokenResponse
import io.craigmiller160.ssoauthserverexp.entity.RefreshToken
import io.craigmiller160.ssoauthserverexp.repository.RefreshTokenRepository
import io.craigmiller160.ssoauthserverexp.security.ClientAuthorities
import io.craigmiller160.ssoauthserverexp.security.ClientUserDetails
import io.craigmiller160.ssoauthserverexp.security.JwtCreator
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OAuth2Service (
        private val jwtCreator: JwtCreator,
        private val refreshTokenRepo: RefreshTokenRepository
) {

    @Secured(ClientAuthorities.CLIENT_CREDENTIALS)
    fun clientCredentials(): TokenResponse {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as ClientUserDetails
        val accessToken = jwtCreator.createAccessToken(userDetails)
        val refreshToken = jwtCreator.createRefreshToken()
        val refreshTokenEntity = RefreshToken(0, refreshToken, LocalDateTime.now())
        refreshTokenRepo.save(refreshTokenEntity)
        return TokenResponse(accessToken, refreshToken)
    }

    @Secured(ClientAuthorities.PASSWORD)
    fun password(): TokenResponse {
        return TokenResponse("password", "")
    }

    @Secured(ClientAuthorities.AUTH_CODE)
    fun authCode(): TokenResponse {
        return TokenResponse("authCode", "")
    }

}