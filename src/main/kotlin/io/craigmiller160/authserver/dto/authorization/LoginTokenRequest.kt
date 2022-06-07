package io.craigmiller160.authserver.dto.authorization

data class LoginTokenRequest(override val username: String, override val password: String) :
  LoginCredentials
