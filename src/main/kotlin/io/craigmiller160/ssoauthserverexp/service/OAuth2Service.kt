package io.craigmiller160.ssoauthserverexp.service

import io.craigmiller160.ssoauthserverexp.dto.TokenRequest
import io.craigmiller160.ssoauthserverexp.dto.TokenResponse
import io.craigmiller160.ssoauthserverexp.entity.RefreshToken
import io.craigmiller160.ssoauthserverexp.exception.InvalidLoginException
import io.craigmiller160.ssoauthserverexp.repository.RefreshTokenRepository
import io.craigmiller160.ssoauthserverexp.repository.RoleRepository
import io.craigmiller160.ssoauthserverexp.repository.UserRepository
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
        private val refreshTokenRepo: RefreshTokenRepository,
        private val userRepo: UserRepository,
        private val roleRepo: RoleRepository
) {

    private fun saveRefreshToken(refreshToken: String) {
        val refreshTokenEntity = RefreshToken(0, refreshToken, LocalDateTime.now())
        refreshTokenRepo.save(refreshTokenEntity)
    }

    @Secured(ClientAuthorities.CLIENT_CREDENTIALS)
    fun clientCredentials(): TokenResponse {
        val clientUserDetails = SecurityContextHolder.getContext().authentication.principal as ClientUserDetails
        val accessToken = jwtCreator.createAccessToken(clientUserDetails)
        val refreshToken = jwtCreator.createRefreshToken()
        saveRefreshToken(refreshToken)
        return TokenResponse(accessToken, refreshToken)
    }

    @Secured(ClientAuthorities.PASSWORD)
    fun password(tokenRequest: TokenRequest): TokenResponse {
        val clientUserDetails = SecurityContextHolder.getContext().authentication.principal as ClientUserDetails
        val user = userRepo.findByEmailAndClientId(tokenRequest.username ?: "", clientUserDetails.client.id)
                ?: throw InvalidLoginException("User does not exist for client")

        // TODO validate password

        val accessToken = jwtCreator.createAccessToken(clientUserDetails, user)
        val refreshToken = jwtCreator.createRefreshToken()
        saveRefreshToken(refreshToken)
        return TokenResponse(accessToken, refreshToken)
    }

    @Secured(ClientAuthorities.AUTH_CODE)
    fun authCode(): TokenResponse {
        return TokenResponse("authCode", "")
    }

}