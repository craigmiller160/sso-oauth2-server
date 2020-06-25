package io.craigmiller160.authserver.dto

data class RefreshTokenData (
        val tokenId: String,
        val grantType: String,
        val clientId: Long,
        val userId: Long?
)