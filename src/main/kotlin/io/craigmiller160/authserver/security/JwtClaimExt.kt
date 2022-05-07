package io.craigmiller160.authserver.security

import com.nimbusds.jwt.JWTClaimsSet
import io.craigmiller160.authserver.dto.access.UserWithClientsAccess

fun JWTClaimsSet.Builder.withUserWithClientsAccess(
  access: UserWithClientsAccess
): JWTClaimsSet.Builder {
  TODO("Finish this")
}
