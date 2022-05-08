package io.craigmiller160.authserver.controller

import arrow.core.Either
import io.craigmiller160.authserver.dto.TokenCookieResponse
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import io.craigmiller160.authserver.function.ReturnUnion2
import io.craigmiller160.authserver.service.AuthorizationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/authorization")
class AuthorizationController(private val authorizationService: AuthorizationService) {
  @PostMapping("/token")
  fun token(@RequestBody request: LoginTokenRequest): Either<Throwable, ResponseEntity<*>> =
    authorizationService.token(request).map(this::handleResponse)

  private fun handleResponse(
    union: ReturnUnion2<TokenResponse, TokenCookieResponse>
  ): ResponseEntity<*> =
    union.fold({ response -> ResponseEntity.ok(response) }, this::handleCookieResponse)

  private fun handleCookieResponse(
    tokenCookieResponse: TokenCookieResponse
  ): ResponseEntity<Nothing> {
    val responseEntityBuilder =
      tokenCookieResponse.redirectUri?.let { ResponseEntity.status(302).header("Location", it) }
        ?: ResponseEntity.noContent()
    return responseEntityBuilder
      .header("Set-Cookie", tokenCookieResponse.accessTokenCookie)
      .header("Set-Cookie", tokenCookieResponse.refreshTokenCookie)
      .build()
  }
}
