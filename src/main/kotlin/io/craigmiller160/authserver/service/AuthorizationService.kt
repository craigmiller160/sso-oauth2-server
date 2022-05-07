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
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.InvalidLoginException
import io.craigmiller160.authserver.function.ReturnUnion2
import io.craigmiller160.authserver.function.TryEither
import io.craigmiller160.authserver.function.rightOrNotFound
import io.craigmiller160.authserver.function.tryEither
import io.craigmiller160.authserver.repository.UserRepository
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
  private val tokenConfig: TokenConfig
) {
  companion object {
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

      val accessToken = createAccessToken(access).bind()
      TODO("Finish this")
    }

  private fun createAccessToken(access: UserWithClientsAccess): TryEither<String> {
    val claims =
      createDefaultClaims(ACCESS_TOKEN_TIMEOUT_SECS)
        .subject(access.email)
        .claim("userId", access.userId)
        .claim("firstName", access.firstName)
        .claim("lastName", access.lastName)
        .claim("clients", access.clients)
        .build()
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

  private fun createDefaultClaims(expSecs: Int): JWTClaimsSet.Builder {
    val now = generateNow()
    return JWTClaimsSet.Builder()
      .issueTime(now)
      .expirationTime(generateExp(expSecs))
      .jwtID(UUID.randomUUID().toString())
      .notBeforeTime(now)
  }

  private fun generateExp(expSecs: Int): Date {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val exp = now.plusSeconds(expSecs.toLong())
    return legacyDateConverter.convertZonedDateTimeToDate(exp)
  }

  private fun generateNow(): Date {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    return legacyDateConverter.convertZonedDateTimeToDate(now)
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
