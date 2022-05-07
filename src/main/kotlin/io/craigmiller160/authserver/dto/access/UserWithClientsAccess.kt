package io.craigmiller160.authserver.dto.access

import com.nimbusds.jwt.JWTClaimsSet

data class UserWithClientsAccess(
  val userId: Long,
  val email: String,
  val firstName: String,
  val lastName: String,
  val clients: Map<String, ClientWithRolesAccess>
) {
  companion object {
    // TODO clean this up if it works
    fun fromClaims(claims: JWTClaimsSet): UserWithClientsAccess =
      UserWithClientsAccess(
        userId = claims.getLongClaim("userId"),
        email = claims.subject,
        firstName = claims.getStringClaim("firstName"),
        lastName = claims.getStringClaim("lastName"),
        clients =
          (claims.getClaim("clients") as Map<String, Map<String, Any>>)
            .map { entry ->
              entry.key to
                ClientWithRolesAccess(
                  clientId = entry.value["clientId"] as Long,
                  clientName = entry.value["clientName"] as String,
                  roles = entry.value["roles"] as List<String>)
            }
            .toMap())
  }
}
