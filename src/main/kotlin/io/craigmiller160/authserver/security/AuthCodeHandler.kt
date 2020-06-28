package io.craigmiller160.authserver.security

import io.craigmiller160.authserver.config.TokenConfig
import org.springframework.stereotype.Component

@Component
class AuthCodeHandler (
        private val tokenConfig: TokenConfig
) {

    fun createAuthCode(): String {
        TODO("Finish this")
    }

}
