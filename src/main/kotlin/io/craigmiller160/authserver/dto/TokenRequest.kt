package io.craigmiller160.authserver.dto

data class TokenRequest(
        val grant_type: String,
        val username: String?,
        val password: String?,
        val refresh_token: String?
)