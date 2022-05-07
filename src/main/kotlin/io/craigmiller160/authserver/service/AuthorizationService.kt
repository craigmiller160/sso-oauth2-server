package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import io.craigmiller160.authserver.function.ReturnUnion2
import org.springframework.stereotype.Service

@Service
class AuthorizationService {
  fun token(request: LoginTokenRequest): ReturnUnion2<TokenResponse, Nothing> {
    TODO("Finish this")
  }
}
