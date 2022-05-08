package io.craigmiller160.authserver.security

import arrow.core.Either
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.authserver.config.TokenConfig
import io.craigmiller160.authserver.dto.access.UserWithClientsAccess
import io.craigmiller160.authserver.dto.access.toClaims
import io.craigmiller160.authserver.function.TryEither
import org.springframework.stereotype.Component

@Component
class AuthorizationJwtHandler(private val tokenConfig: TokenConfig) {

  fun createAccessToken(tokenId: String, access: UserWithClientsAccess): TryEither<String> {
    val claims =
      JWTClaimsSet.parse(
        access.toClaims() + createDefaultClaims(tokenId, tokenConfig.authorization.accessTokenExp))
    return createToken(claims)
  }

  fun createRefreshToken(tokenId: String): TryEither<String> {
    val claims =
      JWTClaimsSet.parse(createDefaultClaims(tokenId, tokenConfig.authorization.refreshTokenExp))
    return createToken(claims)
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
