package io.craigmiller160.authserver.dto.access

@Suppress("UNCHECKED_CAST")
fun UserWithClientsAccess.Companion.fromClaims(claims: Map<String, Any>): UserWithClientsAccess =
  UserWithClientsAccess(
    userId = claims["userId"] as Long,
    email = claims["sub"] as String,
    firstName = claims["firstName"] as String,
    lastName = claims["lastName"] as String,
    clients =
      (claims["clients"] as Map<String, Map<String, Any>>)
        .map { entry -> entry.key to ClientWithRolesAccess.fromClaims(entry.value) }
        .toMap())

fun UserWithClientsAccess.toClaims(): Map<String, Any> =
  mapOf(
    "userId" to userId,
    "sub" to email,
    "firstName" to firstName,
    "lastName" to lastName,
    "clients" to clients.map { entry -> entry.key to entry.value.toClaims() }.toMap())
