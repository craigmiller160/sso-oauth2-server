package io.craigmiller160.authserver.dto

data class TokenResponse (
        val accessToken: String,
        val refreshToken: String,
        val tokenId: String
)
