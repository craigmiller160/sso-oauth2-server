package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.dto.AuthCodeLogin
import io.craigmiller160.authserver.dto.TokenRequest
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.entity.RefreshToken
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.AuthCodeException
import io.craigmiller160.authserver.exception.BadRequestException
import io.craigmiller160.authserver.exception.InvalidLoginException
import io.craigmiller160.authserver.exception.InvalidRefreshTokenException
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.RefreshTokenRepository
import io.craigmiller160.authserver.repository.RoleRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.craigmiller160.authserver.security.AuthCodeHandler
import io.craigmiller160.authserver.security.ClientAuthorities
import io.craigmiller160.authserver.security.ClientUserDetails
import io.craigmiller160.authserver.security.GrantType
import io.craigmiller160.authserver.security.JwtHandler
import org.apache.commons.lang3.StringUtils
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
        private val passwordEncoder: PasswordEncoder,
        private val clientRepo: ClientRepository,
        private val authCodeHandler: AuthCodeHandler
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
        val (accessToken, accessTokenId) = jwtHandler.createAccessToken(clientUserDetails)
        val (refreshToken, refreshTokenId) = jwtHandler.createRefreshToken(clientUserDetails, GrantType.CLIENT_CREDENTIALS, tokenId = accessTokenId)
        saveRefreshToken(refreshToken, refreshTokenId, clientUserDetails.client.id)
        return TokenResponse(accessToken, refreshToken, accessTokenId)
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

        val (accessToken, accessTokenId) = jwtHandler.createAccessToken(clientUserDetails, user, roles)
        val (refreshToken, refreshTokenId) = jwtHandler.createRefreshToken(clientUserDetails, GrantType.PASSWORD, user.id, tokenId = accessTokenId)
        saveRefreshToken(refreshToken, refreshTokenId, clientUserDetails.client.id, user.id)
        return TokenResponse(accessToken, refreshToken, accessTokenId)
    }

    @Secured(ClientAuthorities.AUTH_CODE)
    fun authCode(tokenRequest: TokenRequest): TokenResponse {
        val clientUserDetails = SecurityContextHolder.getContext().authentication.principal as ClientUserDetails
        if (clientUserDetails.client.clientKey != tokenRequest.client_id) {
            throw InvalidLoginException("Invalid client id")
        }

        if (clientUserDetails.client.redirectUri != tokenRequest.redirect_uri) {
            throw InvalidLoginException("Invalid redirect uri")
        }

        val (clientId, userId) = authCodeHandler.validateAuthCode(tokenRequest.code!!)
        if (clientUserDetails.client.id != clientId) {
            throw InvalidLoginException("Invalid auth code client")
        }

        val user = userRepo.findByUserIdAndClientId(userId, clientId)
                ?: throw InvalidLoginException("Invalid user")

        val roles = roleRepo.findAllByUserIdAndClientId(user.id, clientUserDetails.client.id)

        val (accessToken, accessTokenId) = jwtHandler.createAccessToken(clientUserDetails, user, roles)
        val (refreshToken, refreshTokenId) = jwtHandler.createRefreshToken(clientUserDetails, GrantType.AUTH_CODE, user.id, accessTokenId)
        saveRefreshToken(refreshToken, refreshTokenId, clientUserDetails.client.id, user.id)
        return TokenResponse(accessToken, refreshToken, accessTokenId)
    }

    fun authCodeLogin(login: AuthCodeLogin): String {
        val client = clientRepo.findByClientKey(login.clientId)
                ?: throw AuthCodeException("Client not supported")

        val user = userRepo.findByEmailAndClientId(login.username, client.id)
                ?: throw AuthCodeException("User not found")

        if (!passwordEncoder.matches(login.password, user.password)) {
            throw AuthCodeException("Invalid credentials")
        }

        return authCodeHandler.createAuthCode(client.id, user.id, client.authCodeTimeoutSecs!!)
    }

    fun validateAuthCodeLogin(login: AuthCodeLogin) {
        if (StringUtils.isBlank(login.state)) {
            throw AuthCodeException("No state property")
        }

        if (login.responseType != "code") {
            throw AuthCodeException("Invalid response type")
        }

        val client = clientRepo.findByClientKey(login.clientId)
                ?: throw AuthCodeException("Client not supported")

        userRepo.findByEmailAndClientId(login.username, client.id)
                ?: throw AuthCodeException("User not found")

        if (!client.supportsAuthCode(login.redirectUri)) {
            throw AuthCodeException("Client does not support Auth Code")
        }
    }

    @Transactional
    fun refresh(origRefreshToken: String): TokenResponse {
        val clientUserDetails = SecurityContextHolder.getContext().authentication.principal as ClientUserDetails
        val tokenData = jwtHandler.parseRefreshToken(origRefreshToken, clientUserDetails.client.id)

        val existingTokenEntity = refreshTokenRepo.findById(tokenData.tokenId)
                .orElseThrow { InvalidRefreshTokenException("Refresh Token Revoked") }

        refreshTokenRepo.delete(existingTokenEntity)

        val userDataPair: Pair<User,List<Role>>? = tokenData.userId?.let { userId ->
            val user = userRepo.findByUserIdAndClientId(userId, clientUserDetails.client.id)
                    ?: throw InvalidRefreshTokenException("Invalid Refresh User")

            val roles = roleRepo.findAllByUserIdAndClientId(userId, clientUserDetails.client.id)
            Pair(user, roles)
        }

        val (accessToken, accessTokenId) = jwtHandler.createAccessToken(clientUserDetails, userDataPair?.first, userDataPair?.second ?: listOf())
        val (refreshToken, refreshTokenId) = jwtHandler.createRefreshToken(clientUserDetails, tokenData.grantType, tokenData.userId ?: 0, accessTokenId)
        saveRefreshToken(refreshToken, refreshTokenId, tokenData.clientId, tokenData.userId)

        return TokenResponse(accessToken, refreshToken, accessTokenId)
    }

}
