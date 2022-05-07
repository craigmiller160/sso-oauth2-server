package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.dto.TokenCookieResponse
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.function.ReturnUnion2
import io.craigmiller160.authserver.function.TryEither
import io.craigmiller160.authserver.function.rightOrNotFound
import io.craigmiller160.authserver.function.tryEither
import io.craigmiller160.authserver.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
  private val accessLoadingService: AccessLoadingService,
  private val userRepo: UserRepository
) {
  fun token(
    request: LoginTokenRequest
  ): TryEither<ReturnUnion2<TokenResponse, TokenCookieResponse>> =
    tryEither.eager<ReturnUnion2<TokenResponse, TokenCookieResponse>> {
      val user = getUser(request.username).bind()
      TODO("Finish this")
    }

  private fun getUser(username: String): TryEither<User> =
    TryEither.rightOrNotFound(userRepo.findEnabledUserByEmail(username), "User")
}
