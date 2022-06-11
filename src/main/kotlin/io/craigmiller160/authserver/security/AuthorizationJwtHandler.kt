package io.craigmiller160.authserver.security

import arrow.core.Either
import arrow.core.rightIfNotNull
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.authserver.config.TokenConfig
import io.craigmiller160.authserver.dto.access.UserWithClientsAccess
import io.craigmiller160.authserver.dto.access.toClaims
import io.craigmiller160.authserver.exception.InvalidRefreshTokenException
import io.craigmiller160.authserver.function.TryEither
import java.security.interfaces.RSAPublicKey
import org.springframework.stereotype.Component

@Component
class AuthorizationJwtHandler(private val tokenConfig: TokenConfig) {

  fun createAccessToken(tokenId: String, access: UserWithClientsAccess): TryEither<String> {
    val claims =
        JWTClaimsSet.parse(
            access.toClaims() +
                createDefaultClaims(tokenId, tokenConfig.authorization.accessTokenExp))
    return createToken(claims)
  }

  fun createRefreshToken(tokenId: String, overrideExp: Int? = null): TryEither<String> {
    val claims =
        JWTClaimsSet.parse(
            createDefaultClaims(tokenId, overrideExp ?: tokenConfig.authorization.refreshTokenExp))
    return createToken(claims)
  }

  // TODO need tests for all the fail conditions
  fun parseRefreshToken(refreshToken: String): TryEither<String> {
    val jwt = SignedJWT.parse(refreshToken)
    val verifier = RSASSAVerifier(tokenConfig.publicKey as RSAPublicKey)
    if (!jwt.verify(verifier)) {
      return Either.Left(InvalidRefreshTokenException("Bad signature"))
    }

    return jwt.jwtClaimsSet.jwtid.rightIfNotNull {
      InvalidRefreshTokenException("Does not have JWTID")
    }
  }

  private fun createDefaultClaims(tokenId: String, expSecs: Int): Map<String, Any> {
    val now = JwtUtils.generateNow()
    return mapOf(
        "iat" to now.time,
        "exp" to JwtUtils.generateExp(expSecs).time,
        "jti" to tokenId,
        "nbf" to now.time)
  }

  private fun createToken(claims: JWTClaimsSet): TryEither<String> {
    val header = JWSHeader.Builder(JWSAlgorithm.RS256).build()
    val jwt = SignedJWT(header, claims)
    val signer = RSASSASigner(tokenConfig.privateKey)

    return Either.catch {
      jwt.sign(signer)
      jwt.serialize()
    }
  }
}
