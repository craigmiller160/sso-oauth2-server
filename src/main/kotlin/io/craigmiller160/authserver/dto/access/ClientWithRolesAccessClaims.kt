package io.craigmiller160.authserver.dto.access

@Suppress("UNCHECKED_CAST")
fun ClientWithRolesAccess.Companion.fromClaims(claims: Map<String, Any>): ClientWithRolesAccess =
  ClientWithRolesAccess(
    clientId = claims["clientId"] as Long,
    clientName = claims["clientName"] as String,
    roles = claims["roles"] as List<String>)

fun ClientWithRolesAccess.toClaims(): Map<String, Any> =
  mapOf("clientId" to clientId, "clientName" to clientName, "roles" to roles)
