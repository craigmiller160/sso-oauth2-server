package io.craigmiller160.authserver.service

import arrow.core.Either
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.authserver.config.TokenConfig
import io.craigmiller160.authserver.dto.TokenCookieResponse
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.dto.access.UserWithClientsAccess
import io.craigmiller160.authserver.dto.access.toClaims
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import io.craigmiller160.authserver.entity.RefreshToken
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.InvalidLoginException
import io.craigmiller160.authserver.function.ReturnUnion2
import io.craigmiller160.authserver.function.TryEither
import io.craigmiller160.authserver.function.rightOrNotFound
import io.craigmiller160.authserver.function.tryEither
import io.craigmiller160.authserver.repository.RefreshTokenRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.craigmiller160.authserver.security.CookieCreator
import io.craigmiller160.authserver.security.JwtUtils
import io.craigmiller160.date.converter.LegacyDateConverter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
  private val accessLoadingService: AccessLoadingService,
  private val userRepo: UserRepository,
  private val passwordEncoder: PasswordEncoder,
  private val tokenConfig: TokenConfig,
  private val refreshTokenRepo: RefreshTokenRepository
) {
  companion object {
    private const val ACCESS_TOKEN_COOKIE_NAME = "craigmiller160_access_token"
    private const val REFRESH_TOKEN_COOKIE_NAME = "craigmiller160_refresh_token"
    private const val REFRESH_TOKEN_COOKIE_PATH = "/authserver/authentication/refresh"

    // TODO move these to properties
    private const val ACCESS_TOKEN_TIMEOUT_SECS = 60 * 10
    private const val REFRESH_TOKEN_TIMEOUT_SECS = 60 * 60
  }

  private val legacyDateConverter = LegacyDateConverter()

  fun token(
    request: LoginTokenRequest
  ): TryEither<ReturnUnion2<TokenResponse, TokenCookieResponse>> =
    tryEither.eager<ReturnUnion2<TokenResponse, TokenCookieResponse>> {
      val user = validateCredentials(request).bind()
      val access = accessLoadingService.getAccessForUser(user.id).bind()

      val tokenId = UUID.randomUUID().toString()

      val accessToken = createAccessToken(tokenId, access).bind()
      val refreshToken = createRefreshToken(tokenId).bind()
      saveRefreshToken(refreshToken, tokenId, user.id).bind()

      if (request.cookie) {
        val accessTokenCookie = CookieCreator.create(ACCESS_TOKEN_COOKIE_NAME, accessToken)
        val refreshTokenCookie =
          CookieCreator.create(REFRESH_TOKEN_COOKIE_NAME, refreshToken) {
            path = REFRESH_TOKEN_COOKIE_PATH
          }
        ReturnUnion2.ofB(
          TokenCookieResponse(accessTokenCookie, refreshTokenCookie, request.redirectUri))
      } else {
        ReturnUnion2.ofA(TokenResponse(accessToken, refreshToken, tokenId))
      }
    }

  private fun saveRefreshToken(
    refreshToken: String,
    tokenId: String,
    userId: Long
  ): TryEither<Unit> {
    val refreshTokenEntity =
      RefreshToken(tokenId, refreshToken, null, userId, ZonedDateTime.now(ZoneId.of("UTC")))
    return Either.catch { refreshTokenRepo.save(refreshTokenEntity) }
  }

  private fun createRefreshToken(tokenId: String): TryEither<String> {
    val claims = JWTClaimsSet.parse(createDefaultClaims(tokenId, REFRESH_TOKEN_TIMEOUT_SECS))
    return createToken(claims)
  }

  private fun createAccessToken(tokenId: String, access: UserWithClientsAccess): TryEither<String> {
    val claims =
      JWTClaimsSet.parse(
        access.toClaims() + createDefaultClaims(tokenId, ACCESS_TOKEN_TIMEOUT_SECS))
    return createToken(claims)
  }

  private fun createToken(claims: JWTClaimsSet): TryEither<String> {
    val header = JWSHeader.Builder(JWSAlgorithm.RS256).build()
    val jwt = SignedJWT(header, claims)
    val signer = RSASSASigner(tokenConfig.privateKey)

    return Either.catch {
      jwt.sign(signer)
      jwt.serialize()
    }
  }

  private fun createDefaultClaims(tokenId: String, expSecs: Int): Map<String, Any> {
    val now = JwtUtils.generateNow()
    return mapOf(
      "iat" to now.time,
      "exp" to JwtUtils.generateExp(expSecs).time,
      "jti" to tokenId,
      "nbf" to now.time)
  }

  private fun validateCredentials(request: LoginTokenRequest): TryEither<User> =
    tryEither.eager {
      val user = getUser(request.username).bind()
      if (passwordEncoder.matches(request.password, user.password)) {
          Either.Right(user)
        } else {
          Either.Left(InvalidLoginException("Invalid credentials"))
        }
        .bind()
    }

  private fun getUser(username: String): TryEither<User> =
    TryEither.rightOrNotFound(userRepo.findEnabledUserByEmail(username), "User")
}
