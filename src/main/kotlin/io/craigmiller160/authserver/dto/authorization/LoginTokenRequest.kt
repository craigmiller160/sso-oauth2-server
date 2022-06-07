package io.craigmiller160.authserver.dto.authorization

data class LoginTokenRequest(
  val username: String,
  val password: String,
  val cookie: Boolean = false,
  val redirectUri: String? = null
)
