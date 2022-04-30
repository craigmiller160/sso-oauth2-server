package io.craigmiller160.authserver.service

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.leftIfNull
import io.craigmiller160.authserver.dto.access.ClientWithRolesAccess
import io.craigmiller160.authserver.dto.access.UserWithClientsAccess
import io.craigmiller160.authserver.exception.AccessNotFoundException
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.RoleRepository
import io.craigmiller160.authserver.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class AccessLoadingService(
  private val userRepo: UserRepository,
  private val clientRepo: ClientRepository,
  private val roleRepo: RoleRepository
) {

  fun getAccessForUser(userId: Long): Either<Throwable, UserWithClientsAccess> =
    either
      .eager<Throwable, UserWithClientsAccess> {
        val user =
          Either.catch { userRepo.findEnabledUserById(userId) }
            .leftIfNull { AccessNotFoundException("Could not find User for ID: $userId") }
            .bind()
        val clients = Either.catch { clientRepo.findAllEnabledClientsByUserId(userId) }.bind()
        val roles = Either.catch { roleRepo.findAllByUserId(userId) }.bind()

        val userClientsMap =
          clients.associate { client ->
            val clientRoles = roles.filter { role -> role.clientId == client.id }
            val clientWithRolesAccess =
              ClientWithRolesAccess(
                clientId = client.id, clientName = client.name, roles = clientRoles.map { it.name })
            client.clientKey to clientWithRolesAccess
          }

        UserWithClientsAccess(
          userId = userId,
          email = user.email,
          firstName = user.firstName,
          lastName = user.lastName,
          clients = userClientsMap)
      }
      .mapLeft { ex ->
        AccessNotFoundException("Error getting access for User with ID: $userId", ex)
      }
}
