package io.craigmiller160.ssoauthserverexp.security

import io.craigmiller160.ssoauthserverexp.config.TokenConfig
import io.craigmiller160.ssoauthserverexp.entity.User
import org.springframework.stereotype.Component

// TODO delete if unnecessary
@Component
class JwtCreator(private val tokenConfig: TokenConfig) {

    fun createJwt(user: User): String {
        TODO("Finish this")
    }

}