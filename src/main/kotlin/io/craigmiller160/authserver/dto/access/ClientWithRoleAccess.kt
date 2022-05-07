package io.craigmiller160.authserver.dto.access

data class ClientWithRolesAccess(
  val clientId: Long,
  val clientName: String,
  val roles: List<String>
)
