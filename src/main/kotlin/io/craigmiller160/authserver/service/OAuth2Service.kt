/*
 *     sso-oauth2-server
 *     Copyright (C) 2020 Craig Miller
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.dto.AuthCodeLogin
import io.craigmiller160.authserver.dto.TokenRequest
import io.craigmiller160.authserver.dto.tokenResponse.TokenResponse
import io.craigmiller160.authserver.entity.RefreshToken
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.AuthCodeException
import io.craigmiller160.authserver.exception.InvalidLoginException
import io.craigmiller160.authserver.exception.InvalidRefreshTokenException
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.RefreshTokenRepository
import io.craigmiller160.authserver.repository.RoleRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.craigmiller160.authserver.security.AuthCodeHandler
import io.craigmiller160.authserver.security.GrantType
import io.craigmiller160.authserver.security.OAuth2ClientUserDetails
import io.craigmiller160.authserver.security.OAuth2JwtHandler
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.transaction.Transactional
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class OAuth2Service(
    private val OAuth2JwtHandler: OAuth2JwtHandler,
    private val refreshTokenRepo: RefreshTokenRepository,
    private val userRepo: UserRepository,
    private val roleRepo: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val clientRepo: ClientRepository,
    private val authCodeHandler: AuthCodeHandler
) {

  private fun saveRefreshToken(
      refreshToken: String,
      tokenId: String,
      clientId: Long,
      userId: Long? = null
  ) {
    val refreshTokenEntity =
        RefreshToken(tokenId, refreshToken, clientId, userId, ZonedDateTime.now(ZoneId.of("UTC")))
    refreshTokenRepo.save(refreshTokenEntity)
  }

  @Transactional
  fun clientCredentials(): TokenResponse {
    val OAuth2ClientUserDetails =
        SecurityContextHolder.getContext().authentication.principal as OAuth2ClientUserDetails
    val (accessToken, accessTokenId) = OAuth2JwtHandler.createAccessToken(OAuth2ClientUserDetails)
    val (refreshToken, refreshTokenId) =
        OAuth2JwtHandler.createRefreshToken(
            OAuth2ClientUserDetails, GrantType.CLIENT_CREDENTIALS, tokenId = accessTokenId)
    saveRefreshToken(refreshToken, refreshTokenId, OAuth2ClientUserDetails.client.id)
    return TokenResponse(accessToken, refreshToken, accessTokenId)
  }

  @Transactional
  fun password(tokenRequest: TokenRequest): TokenResponse {
    val OAuth2ClientUserDetails =
        SecurityContextHolder.getContext().authentication.principal as OAuth2ClientUserDetails
    val user =
        userRepo.findByEmailAndClientId(
            tokenRequest.username ?: "", OAuth2ClientUserDetails.client.id)
            ?: throw InvalidLoginException("User does not exist for client")

    if (!user.enabled) {
      throw InvalidLoginException("User is disabled")
    }

    if (!passwordEncoder.matches(tokenRequest.password, user.password)) {
      throw InvalidLoginException("Invalid credentials")
    }

    val roles = roleRepo.findAllByUserIdAndClientId(user.id, OAuth2ClientUserDetails.client.id)

    val (accessToken, accessTokenId) =
        OAuth2JwtHandler.createAccessToken(OAuth2ClientUserDetails, user, roles)
    val (refreshToken, refreshTokenId) =
        OAuth2JwtHandler.createRefreshToken(
            OAuth2ClientUserDetails, GrantType.PASSWORD, user.id, tokenId = accessTokenId)
    saveRefreshToken(refreshToken, refreshTokenId, OAuth2ClientUserDetails.client.id, user.id)
    return TokenResponse(accessToken, refreshToken, accessTokenId)
  }

  @Transactional
  fun authCode(tokenRequest: TokenRequest): TokenResponse {
    val OAuth2ClientUserDetails =
        SecurityContextHolder.getContext().authentication.principal as OAuth2ClientUserDetails
    if (OAuth2ClientUserDetails.client.clientKey != tokenRequest.client_id) {
      throw InvalidLoginException("Invalid client id")
    }

    if (!OAuth2ClientUserDetails.client.getRedirectUris().contains(tokenRequest.redirect_uri)) {
      throw InvalidLoginException("Invalid redirect uri")
    }

    val (clientId, userId) = authCodeHandler.validateAuthCode(tokenRequest.code!!)
    if (OAuth2ClientUserDetails.client.id != clientId) {
      throw InvalidLoginException("Invalid auth code client")
    }

    val user =
        userRepo.findByUserIdAndClientId(userId, clientId)
            ?: throw InvalidLoginException("Invalid user")

    if (!user.enabled) {
      throw InvalidLoginException("User is disabled")
    }

    val roles = roleRepo.findAllByUserIdAndClientId(user.id, OAuth2ClientUserDetails.client.id)

    val (accessToken, accessTokenId) =
        OAuth2JwtHandler.createAccessToken(OAuth2ClientUserDetails, user, roles)
    val (refreshToken, refreshTokenId) =
        OAuth2JwtHandler.createRefreshToken(
            OAuth2ClientUserDetails, GrantType.AUTH_CODE, user.id, accessTokenId)
    saveRefreshToken(refreshToken, refreshTokenId, OAuth2ClientUserDetails.client.id, user.id)
    return TokenResponse(accessToken, refreshToken, accessTokenId)
  }

  @Transactional
  fun authCodeLogin(login: AuthCodeLogin): String {
    val client =
        clientRepo.findByClientKey(login.clientId)
            ?: throw AuthCodeException("Client not supported")

    val user =
        userRepo.findByEmailAndClientId(login.username, client.id)
            ?: throw AuthCodeException("User not found")

    if (!passwordEncoder.matches(login.password, user.password)) {
      throw AuthCodeException("Invalid credentials")
    }

    return authCodeHandler.createAuthCode(client.id, user.id, client.authCodeTimeoutSecs)
  }

  @Transactional
  fun validateAuthCodeLogin(login: AuthCodeLogin) {
    if (login.state.isBlank()) {
      throw AuthCodeException("No state property")
    }

    if (login.responseType != "code") {
      throw AuthCodeException("Invalid response type")
    }

    val client =
        clientRepo.findByClientKey(login.clientId)
            ?: throw AuthCodeException("Client not supported")

    val user =
        userRepo.findByEmailAndClientId(login.username, client.id)
            ?: throw AuthCodeException("User not found")

    if (!user.enabled) {
      throw AuthCodeException("User is disabled")
    }

    if (!client.supportsAuthCode(login.redirectUri)) {
      throw AuthCodeException("Client does not support Auth Code")
    }
  }

  @Transactional
  fun refresh(origRefreshToken: String): TokenResponse {
    val OAuth2ClientUserDetails =
        SecurityContextHolder.getContext().authentication.principal as OAuth2ClientUserDetails
    val tokenData =
        OAuth2JwtHandler.parseRefreshToken(origRefreshToken, OAuth2ClientUserDetails.client.id)

    refreshTokenRepo.findById(tokenData.tokenId).orElseThrow {
      InvalidRefreshTokenException("Refresh Token Revoked")
    }

    val userDataPair: Pair<User, List<Role>>? =
        tokenData.userId?.let { userId ->
          val user =
              userRepo.findByUserIdAndClientId(userId, OAuth2ClientUserDetails.client.id)
                  ?: throw InvalidRefreshTokenException("Invalid Refresh User")

          if (!user.enabled) {
            throw InvalidLoginException("User is disabled")
          }

          val roles = roleRepo.findAllByUserIdAndClientId(userId, OAuth2ClientUserDetails.client.id)
          Pair(user, roles)
        }

    val (accessToken, accessTokenId) =
        OAuth2JwtHandler.createAccessToken(
            OAuth2ClientUserDetails,
            userDataPair?.first,
            userDataPair?.second ?: listOf(),
            tokenData.tokenId)
    val (refreshToken, refreshTokenId) =
        OAuth2JwtHandler.createRefreshToken(
            OAuth2ClientUserDetails, tokenData.grantType, tokenData.userId ?: 0, accessTokenId)
    saveRefreshToken(refreshToken, refreshTokenId, tokenData.clientId, tokenData.userId)

    return TokenResponse(accessToken, refreshToken, accessTokenId)
  }
}
