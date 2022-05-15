package io.craigmiller160.authserver.controller

import io.craigmiller160.authserver.dto.TokenCookieResponse
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import io.craigmiller160.authserver.function.toResponseEntity
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
  fun token(@RequestBody request: LoginTokenRequest): ResponseEntity<*> =
    authorizationService
      .token(request)
      .map { it.fold({ response -> ResponseEntity.ok(response) }, this::handleCookieResponse) }
      .toResponseEntity()

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
