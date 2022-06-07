package io.craigmiller160.authserver.dto.access

data class UserWithClientsAccess(
    val userId: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val clients: Map<String, ClientWithRolesAccess>
) {
  companion object {}
}
