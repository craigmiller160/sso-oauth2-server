package io.craigmiller160.ssoauthserverexp.dto

data class TokenResponse (
        val accessToken: String,
        val refreshToken: String
)