package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.dto.TokenRequest
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.entity.RefreshToken
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.InvalidLoginException
import io.craigmiller160.authserver.exception.InvalidRefreshTokenException
import io.craigmiller160.authserver.repository.RefreshTokenRepository
import io.craigmiller160.authserver.repository.RoleRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.craigmiller160.authserver.security.ClientAuthorities
import io.craigmiller160.authserver.security.ClientUserDetails
import io.craigmiller160.authserver.security.GrantType
import io.craigmiller160.authserver.security.JwtHandler
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class OAuth2Service (
        private val jwtHandler: JwtHandler,
        private val refreshTokenRepo: RefreshTokenRepository,
        private val userRepo: UserRepository,
        private val roleRepo: RoleRepository,
        private val passwordEncoder: PasswordEncoder
) {

    private fun saveRefreshToken(refreshToken: String, tokenId: String, clientId: Long, userId: Long? = null) {
        val refreshTokenEntity = RefreshToken(tokenId, refreshToken, clientId, userId, LocalDateTime.now())
        userId?.let { refreshTokenRepo.removeClientUserRefresh(clientId, userId) }
                ?: refreshTokenRepo.removeClientOnlyRefresh(clientId)
        refreshTokenRepo.save(refreshTokenEntity)
    }

    @Secured(ClientAuthorities.CLIENT_CREDENTIALS)
    @Transactional
    fun clientCredentials(): TokenResponse {
        val clientUserDetails = SecurityContextHolder.getContext().authentication.principal as ClientUserDetails
        val accessToken = jwtHandler.createAccessToken(clientUserDetails)
        val (refreshToken, tokenId) = jwtHandler.createRefreshToken(clientUserDetails, GrantType.CLIENT_CREDENTIALS)
        saveRefreshToken(refreshToken, tokenId, clientUserDetails.client.id)
        return TokenResponse(accessToken, refreshToken)
    }

    @Secured(ClientAuthorities.PASSWORD)
    @Transactional
    fun password(tokenRequest: TokenRequest): TokenResponse {
        val clientUserDetails = SecurityContextHolder.getContext().authentication.principal as ClientUserDetails
        val user = userRepo.findByEmailAndClientId(tokenRequest.username ?: "", clientUserDetails.client.id)
                ?: throw InvalidLoginException("User does not exist for client")

        if (!passwordEncoder.matches(tokenRequest.password, user.password)) {
            throw InvalidLoginException("Invalid credentials")
        }

        val roles = roleRepo.findAllByUserIdAndClientId(user.id, clientUserDetails.client.id)

        val accessToken = jwtHandler.createAccessToken(clientUserDetails, user, roles)
        val (refreshToken, tokenId) = jwtHandler.createRefreshToken(clientUserDetails, GrantType.PASSWORD, user.id)
        saveRefreshToken(refreshToken, tokenId, clientUserDetails.client.id, user.id)
        return TokenResponse(accessToken, refreshToken)
    }

    @Secured(ClientAuthorities.AUTH_CODE)
    fun authCode(): TokenResponse {
        return TokenResponse("authCode", "")
    }

    @Transactional
    fun refresh(origRefreshToken: String): TokenResponse {
        val clientUserDetails = SecurityContextHolder.getContext().authentication.principal as ClientUserDetails
        val tokenData = jwtHandler.parseRefreshToken(origRefreshToken, clientUserDetails.client.id)

        val existingTokenEntity = refreshTokenRepo.findById(tokenData.tokenId)
                .orElseThrow { InvalidRefreshTokenException("Refresh Token Revoked") }

        refreshTokenRepo.delete(existingTokenEntity)

        val userDataPair: Pair<User,List<Role>>? = tokenData.userId?.let { userId ->
            val user = userRepo.findById(userId)
                    .orElseThrow { InvalidRefreshTokenException("Invalid Refresh UserID") }

            val roles = roleRepo.findAllByUserIdAndClientId(userId, clientUserDetails.client.id)
            Pair(user, roles)
        }

        val accessToken = jwtHandler.createAccessToken(clientUserDetails, userDataPair?.first, userDataPair?.second ?: listOf())
        val (refreshToken, tokenId) = jwtHandler.createRefreshToken(clientUserDetails, tokenData.grantType, tokenData.userId ?: 0)
        saveRefreshToken(refreshToken, tokenId, tokenData.clientId, tokenData.userId)

        return TokenResponse(accessToken, refreshToken)
    }

}