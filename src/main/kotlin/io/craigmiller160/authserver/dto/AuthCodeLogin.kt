package io.craigmiller160.authserver.dto

data class AuthCodeLogin (
        val username: String,
        val password: String,
        val clientId: String,
        val redirectUri: String,
        val responseType: String,
        val state: String
)
