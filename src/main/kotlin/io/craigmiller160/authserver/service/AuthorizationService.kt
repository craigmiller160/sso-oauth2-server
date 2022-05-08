package io.craigmiller160.authserver.service

import arrow.core.Either
import io.craigmiller160.authserver.dto.TokenCookieResponse
import io.craigmiller160.authserver.dto.TokenResponse
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
import io.craigmiller160.authserver.security.AuthorizationJwtHandler
import io.craigmiller160.authserver.security.CookieCreator
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
  private val refreshTokenRepo: RefreshTokenRepository,
  private val jwtHandler: AuthorizationJwtHandler
) {
  companion object {
    private const val ACCESS_TOKEN_COOKIE_NAME = "craigmiller160_access_token"
    private const val REFRESH_TOKEN_COOKIE_NAME = "craigmiller160_refresh_token"
    private const val REFRESH_TOKEN_COOKIE_PATH = "/authserver/authentication/refresh"
  }

  fun token(
    request: LoginTokenRequest
  ): TryEither<ReturnUnion2<TokenResponse, TokenCookieResponse>> =
    tryEither.eager<ReturnUnion2<TokenResponse, TokenCookieResponse>> {
      val user = validateCredentials(request).bind()
      val access = accessLoadingService.getAccessForUser(user.id).bind()

      val tokenId = UUID.randomUUID().toString()

      val accessToken = jwtHandler.createAccessToken(tokenId, access).bind()
      val refreshToken = jwtHandler.createRefreshToken(tokenId).bind()
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
