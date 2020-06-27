package io.craigmiller160.authserver.dto

data class AuthCodeLogin (
        val username: String,
        val password: String,
        var clientId: String,
        var redirectUri: String
)
