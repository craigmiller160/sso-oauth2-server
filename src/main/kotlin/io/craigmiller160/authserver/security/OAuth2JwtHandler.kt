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

package io.craigmiller160.authserver.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.authserver.config.TokenConfig
import io.craigmiller160.authserver.dto.RefreshTokenData
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.BadRequestException
import io.craigmiller160.authserver.exception.InvalidRefreshTokenException
import io.craigmiller160.authserver.exception.UnsupportedGrantTypeException
import io.craigmiller160.date.converter.LegacyDateConverter
import java.security.interfaces.RSAPublicKey
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class OAuth2JwtHandler(private val tokenConfig: TokenConfig) {

  fun createAccessToken(
    OAuth2ClientUserDetails: OAuth2ClientUserDetails,
    user: User? = null,
    roles: List<Role> = listOf(),
    existingTokenId: String? = null
  ): Pair<String, String> {
    val roleNames = roles.map { it.name }

    var claimBuilder =
      createDefaultClaims(OAuth2ClientUserDetails.client.accessTokenTimeoutSecs)
        .claim("clientKey", OAuth2ClientUserDetails.username)
        .claim("clientName", OAuth2ClientUserDetails.client.name)
        .claim("roles", roleNames)

    claimBuilder = existingTokenId?.let { claimBuilder.jwtID(existingTokenId) } ?: claimBuilder

    claimBuilder =
      user?.let {
        claimBuilder
          .subject(user.email)
          .claim("userId", user.id)
          .claim("userEmail", user.email)
          .claim("firstName", user.firstName)
          .claim("lastName", user.lastName)
      }
        ?: claimBuilder.subject(OAuth2ClientUserDetails.client.name)

    val claims = claimBuilder.build()
    val token = createToken(claims)
    return Pair(token, claims.jwtid)
  }

  private fun createDefaultClaims(expSecs: Int): JWTClaimsSet.Builder {
    val now = JwtUtils.generateNow()
    return JWTClaimsSet.Builder()
      .issueTime(now)
      .expirationTime(JwtUtils.generateExp(expSecs))
      .jwtID(UUID.randomUUID().toString())
      .notBeforeTime(now)
  }

  private fun createToken(claims: JWTClaimsSet): String {
    val header = JWSHeader.Builder(JWSAlgorithm.RS256).build()
    val jwt = SignedJWT(header, claims)
    val signer = RSASSASigner(tokenConfig.privateKey)

    jwt.sign(signer)
    return jwt.serialize()
  }

  fun createRefreshToken(
    OAuth2ClientUserDetails: OAuth2ClientUserDetails,
    grantType: String,
    userId: Long = 0,
    tokenId: String
  ): Pair<String, String> {
    var claimBuilder =
      createDefaultClaims(OAuth2ClientUserDetails.client.refreshTokenTimeoutSecs)
        .claim("grantType", grantType)
        .claim("clientId", OAuth2ClientUserDetails.client.id)
        .jwtID(tokenId)

    if (userId > 0) {
      claimBuilder = claimBuilder.claim("userId", userId)
    }

    val claims = claimBuilder.build()

    val token = createToken(claims)
    return Pair(token, claims.jwtid)
  }

  fun parseRefreshToken(refreshToken: String, clientId: Long): RefreshTokenData {
    val jwt = SignedJWT.parse(refreshToken)
    val verifier = RSASSAVerifier(tokenConfig.publicKey as RSAPublicKey)
    if (!jwt.verify(verifier)) {
      throw InvalidRefreshTokenException("Bad Signature")
    }

    val claims = jwt.jwtClaimsSet
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val exp =
      LegacyDateConverter().convertDateToZonedDateTime(claims.expirationTime, ZoneId.of("UTC"))
    if (exp < now) {
      throw InvalidRefreshTokenException("Expired")
    }

    val refreshClientId = claims.getLongClaim("clientId")
    if (refreshClientId != clientId) {
      throw InvalidRefreshTokenException("Invalid Client ID")
    }

    val userId = claims.getLongClaim("userId")
    if (userId == null || userId <= 0) {
      throw BadRequestException("Refresh token has no user id")
    }
    val grantType = claims.getStringClaim("grantType")
    if (!GrantType.isGrantTypeSupported(grantType)) {
      throw UnsupportedGrantTypeException(grantType)
    }
    val tokenId = claims.jwtid

    return RefreshTokenData(tokenId, grantType, refreshClientId, userId)
  }
}
