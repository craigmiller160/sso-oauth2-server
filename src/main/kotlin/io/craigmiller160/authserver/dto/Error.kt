package io.craigmiller160.authserver.dto

import java.time.LocalDateTime

data class Error (
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val status: Int = 0,
        val error: String = "",
        val message: String = "",
        val path: String = ""
)