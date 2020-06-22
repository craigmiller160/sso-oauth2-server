package io.craigmiller160.ssoauthserverexp.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.ssoauthserverexp.config.TokenConfig
import io.craigmiller160.ssoauthserverexp.util.LegacyDateConverter
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
        // TODO add claims
        val claims = JWTClaimsSet.Builder()
                .issueTime(Date())
                .expirationTime(generateExp(tokenConfig.accessExpSecs))
                .jwtID(UUID.randomUUID().toString())
                .notBeforeTime(Date())
                .build()

        val header = JWSHeader.Builder(JWSAlgorithm.RS256)
                .build()
        val jwt = SignedJWT(header, claims)
        val signer = RSASSASigner(tokenConfig.privateKey)

        jwt.sign(signer)
        return jwt.serialize()
    }

}