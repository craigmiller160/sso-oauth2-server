package io.craigmiller160.authserver.dto.authorization

data class TokenRefreshRequest(val refreshToken: String, val cookie: Boolean?)
