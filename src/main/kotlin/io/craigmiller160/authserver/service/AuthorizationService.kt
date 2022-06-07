package io.craigmiller160.authserver.service

import arrow.core.Either
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import io.craigmiller160.authserver.dto.authorization.TokenRefreshRequest
import io.craigmiller160.authserver.dto.tokenResponse.TokenCookieResponse
import io.craigmiller160.authserver.dto.tokenResponse.TokenResponse
import io.craigmiller160.authserver.dto.tokenResponse.TokenValues
import io.craigmiller160.authserver.entity.RefreshToken
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.InvalidLoginException
import io.craigmiller160.authserver.function.TryEither
import io.craigmiller160.authserver.function.tryEither
import io.craigmiller160.authserver.repository.RefreshTokenRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.craigmiller160.authserver.security.ACCESS_TOKEN_COOKIE_NAME
import io.craigmiller160.authserver.security.AuthorizationJwtHandler
import io.craigmiller160.authserver.security.CookieCreator
import io.craigmiller160.authserver.security.REFRESH_TOKEN_COOKIE_NAME
import io.craigmiller160.authserver.security.REFRESH_TOKEN_COOKIE_PATH
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import javax.transaction.Transactional
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

  @Transactional
  fun token(request: LoginTokenRequest): TryEither<TokenValues> =
      tryEither.eager {
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
          TokenCookieResponse(
              accessToken = accessToken,
              refreshToken = refreshToken,
              tokenId = tokenId,
              accessTokenCookie = accessTokenCookie,
              refreshTokenCookie = refreshTokenCookie,
              redirectUri = request.redirectUri)
        } else {
          TokenResponse(accessToken, refreshToken, tokenId)
        }
      }

  fun refresh(request: TokenRefreshRequest): TryEither<TokenValues> =
      tryEither.eager { TODO("Finish this") }

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
      Either.fromNullable(userRepo.findEnabledUserByEmail(username)).mapLeft {
        InvalidLoginException("User not found")
      }
}
