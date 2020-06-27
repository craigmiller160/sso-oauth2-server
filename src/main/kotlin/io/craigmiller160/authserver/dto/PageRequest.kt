package io.craigmiller160.authserver.dto

data class PageRequest (
        val client_id: String,
        val redirect_uri: String,
        val response_type: String
)
