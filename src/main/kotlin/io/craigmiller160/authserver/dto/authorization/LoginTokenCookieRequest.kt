package io.craigmiller160.authserver.dto.authorization

data class LoginTokenCookieRequest(
  override val username: String,
  override val password: String,
  val redirectUri: String? = null
) : LoginCredentials
