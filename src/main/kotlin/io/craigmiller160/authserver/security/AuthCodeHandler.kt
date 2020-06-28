package io.craigmiller160.authserver.security

import io.craigmiller160.authserver.config.TokenConfig
import org.springframework.stereotype.Component

@Component
class AuthCodeHandler (
        private val tokenConfig: TokenConfig
) {

    fun createAuthCode(clientId: Long, userId: Long): String {
        val exp = System.currentTimeMillis()
        TODO("Finish this")
    }

    fun validateAuthCode(authCode: String): Pair<Long,Long> {
        // TODO validate that it isn't expired and extract the client ID and user ID from it
        TODO("Finish this")
    }

}
