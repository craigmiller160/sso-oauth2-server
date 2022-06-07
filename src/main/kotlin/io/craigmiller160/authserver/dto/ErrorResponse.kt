package io.craigmiller160.authserver.dto

import java.time.ZonedDateTime

data class ErrorResponse(
    val timestamp: ZonedDateTime,
    val status: Int,
    val message: String,
    val method: String,
    val uri: String
)
