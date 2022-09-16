package io.craigmiller160.authserver.controller

import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import io.craigmiller160.authserver.dto.authorization.TokenRefreshRequest
import io.craigmiller160.authserver.dto.tokenResponse.TokenCookieResponse
import io.craigmiller160.authserver.dto.tokenResponse.TokenResponse
import io.craigmiller160.authserver.dto.tokenResponse.TokenValues
import io.craigmiller160.authserver.function.TryEither
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
  fun token(@RequestBody request: LoginTokenRequest): TryEither<ResponseEntity<TokenResponse>> =
    authorizationService.token(request).map(::buildResponse)

  private fun buildResponse(tokenValues: TokenValues): ResponseEntity<TokenResponse> =
    when (tokenValues) {
      is TokenResponse -> ResponseEntity.ok(tokenValues)
      is TokenCookieResponse -> handleCookieResponse(tokenValues)
    }

  private fun handleCookieResponse(
    tokenCookieResponse: TokenCookieResponse
  ): ResponseEntity<TokenResponse> {
    val responseEntityBuilder =
      tokenCookieResponse.redirectUri?.let { ResponseEntity.status(302).header("Location", it) }
        ?: ResponseEntity.status(200)
    return responseEntityBuilder
      .header("Set-Cookie", tokenCookieResponse.accessTokenCookie)
      .header("Set-Cookie", tokenCookieResponse.refreshTokenCookie)
      .body(tokenCookieResponse.tokenResponse)
  }

  @PostMapping("/refresh")
  fun refresh(@RequestBody request: TokenRefreshRequest): TryEither<ResponseEntity<TokenResponse>> =
    authorizationService.refresh(request).map(::buildResponse)

  @PostMapping("/logout")
  fun logout(): ResponseEntity<Nothing> {
    val cookies = authorizationService.logout()
    return cookies
      .fold(ResponseEntity.noContent()) { builder, cookie -> builder.header("Set-Cookie", cookie) }
      .build()
  }
}
