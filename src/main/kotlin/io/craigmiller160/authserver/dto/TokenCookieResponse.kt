package io.craigmiller160.authserver.dto

data class TokenCookieResponse(
  val accessTokenCookie: String,
  val refreshTokenCookie: String,
  val redirectUri: String?
)
