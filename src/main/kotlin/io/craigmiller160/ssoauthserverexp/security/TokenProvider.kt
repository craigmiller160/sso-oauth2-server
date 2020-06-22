package io.craigmiller160.ssoauthserverexp.security

import io.craigmiller160.ssoauthserverexp.entity.User
import org.springframework.security.core.Authentication
import javax.servlet.http.HttpServletRequest

interface TokenProvider {

    fun createToken(user: User, params: Map<String,Any> = mapOf()): String

    fun resolveToken(req: HttpServletRequest): String?

    fun validateToken(token: String, params: Map<String,Any> = mapOf()): TokenValidationStatus

    fun createAuthentication(token: String): Authentication

    fun getClaims(token: String): Map<String,Any>

    fun isRefreshAllowed(user: User): Boolean

}