package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.dto.TokenCookieResponse
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import io.craigmiller160.authserver.function.ReturnUnion2
import io.craigmiller160.authserver.function.TryEither
import org.springframework.stereotype.Service

@Service
class AuthorizationService(private val accessLoadingService: AccessLoadingService) {
  fun token(
    request: LoginTokenRequest
  ): TryEither<ReturnUnion2<TokenResponse, TokenCookieResponse>> {
    TODO("Finish this")
  }
}
