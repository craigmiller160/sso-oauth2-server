package io.craigmiller160.authserver.service

import arrow.core.Either
import arrow.core.computations.ResultEffect.bind
import arrow.core.computations.either
import arrow.core.flatMap
import io.craigmiller160.authserver.dto.access.ClientWithRolesAccess
import io.craigmiller160.authserver.dto.access.UserWithClientsAccess
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.AccessNotFoundException
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.RoleRepository
import io.craigmiller160.authserver.repository.UserRepository
import org.springframework.stereotype.Service

private fun createGetUserById(userRepo: UserRepository): (Long) -> Either<Throwable,User> {
    return fun(userId: Long): Either<Throwable,User> =
            Either.catch { userRepo.findEnabledUserById(userId) }
                    .flatMap { user ->
                        user?.let { Either.Right(it) }
                                ?: Either.Left(AccessNotFoundException("No user for ID: $userId"))
                    }
}

@Service
class AccessLoadingService(
        private val userRepo: UserRepository,
        private val clientRepo: ClientRepository,
        private val roleRepo: RoleRepository
) {
    private val getUserById = createGetUserById(userRepo)

    fun getAccessForUser(userId: Long): Either<Throwable, UserWithClientsAccess> =
            either.eager {
                val user = getUserById(userId).bind()
                val clients = clientRepo.findAllEnabledClientsByUserId(userId)
                val roles = roleRepo.findAllByUserId(userId)

                val userClientsMap = clients.associate { client ->
                    val clientRoles = roles.filter { role -> role.clientId == client.id }
                    val clientWithRolesAccess = ClientWithRolesAccess(
                            clientId = client.id,
                            clientName = client.name,
                            roles = clientRoles.map { it.name }
                    )
                    client.clientKey to clientWithRolesAccess
                }

                UserWithClientsAccess(
                        userId = userId,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        clients = userClientsMap
                )
            }

}