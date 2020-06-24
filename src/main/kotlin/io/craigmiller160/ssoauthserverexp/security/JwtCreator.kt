package io.craigmiller160.ssoauthserverexp.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.ssoauthserverexp.config.TokenConfig
import io.craigmiller160.ssoauthserverexp.util.LegacyDateConverter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

@Component
class JwtCreator(
        private val tokenConfig: TokenConfig,
        private val legacyDateConverter: LegacyDateConverter
) {

    private fun generateExp(expSecs: Int): Date {
        val now = LocalDateTime.now()
        val exp = now.plusSeconds(expSecs.toLong())
        return legacyDateConverter.convertLocalDateTimeToDate(exp)
    }

    fun createAccessToken(): String {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as ClientUserDetails
        val claims = createDefaultClaims(tokenConfig.accessExpSecs)
                .claim("clientKey", userDetails.username)
                .build()

        return createToken(claims)
    }

    private fun createDefaultClaims(expSecs: Int): JWTClaimsSet.Builder {
        return JWTClaimsSet.Builder()
                .issueTime(Date())
                .expirationTime(generateExp(expSecs))
                .jwtID(UUID.randomUUID().toString())
                .notBeforeTime(Date())
    }

    private fun createToken(claims: JWTClaimsSet): String {
        val header = JWSHeader.Builder(JWSAlgorithm.RS256)
                .build()
        val jwt = SignedJWT(header, claims)
        val signer = RSASSASigner(tokenConfig.privateKey)

        jwt.sign(signer)
        return jwt.serialize()
    }

    fun createRefreshToken(): String {
        val claims = createDefaultClaims(tokenConfig.refreshExpSecs)
                .build()

        return createToken(claims)
    }

}