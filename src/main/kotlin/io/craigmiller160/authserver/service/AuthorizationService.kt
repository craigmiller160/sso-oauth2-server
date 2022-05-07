package io.craigmiller160.authserver.service

import arrow.core.Either
import io.craigmiller160.authserver.dto.TokenCookieResponse
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.InvalidLoginException
import io.craigmiller160.authserver.function.ReturnUnion2
import io.craigmiller160.authserver.function.TryEither
import io.craigmiller160.authserver.function.rightOrNotFound
import io.craigmiller160.authserver.function.tryEither
import io.craigmiller160.authserver.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
  private val accessLoadingService: AccessLoadingService,
  private val userRepo: UserRepository,
  private val passwordEncoder: PasswordEncoder
) {
  fun token(
    request: LoginTokenRequest
  ): TryEither<ReturnUnion2<TokenResponse, TokenCookieResponse>> =
    tryEither.eager<ReturnUnion2<TokenResponse, TokenCookieResponse>> {
      val user = validateCredentials(request).bind()
      val access = accessLoadingService.getAccessForUser(user.id).bind()
      TODO("Finish this")
    }

  private fun validateCredentials(request: LoginTokenRequest): TryEither<User> =
    tryEither.eager {
      val user = getUser(request.username).bind()
      if (passwordEncoder.matches(request.password, user.password)) {
          Either.Right(user)
        } else {
          Either.Left(InvalidLoginException(""))
        }
        .bind()
    }

  private fun getUser(username: String): TryEither<User> =
    TryEither.rightOrNotFound(userRepo.findEnabledUserByEmail(username), "User")
}
