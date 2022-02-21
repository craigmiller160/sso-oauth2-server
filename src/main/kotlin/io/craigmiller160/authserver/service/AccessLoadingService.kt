package io.craigmiller160.authserver.service

import arrow.core.Either
import arrow.core.computations.ResultEffect.bind
import arrow.core.computations.either
import arrow.core.flatMap
import io.craigmiller160.authserver.dto.access.ClientWithRolesAccess
import io.craigmiller160.authserver.dto.access.UserWithClientsAccess
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.Role
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

private fun createGetClients(clientRepo: ClientRepository): (Long,Long?) -> Either<Throwable,List<Client>> {
    return fun(userId: Long, clientId: Long?): Either<Throwable,List<Client>> =
            clientId?.let { actualClientId ->
                clientRepo.findEnabledClientByUserIdAndClientId(userId, actualClientId)
                        ?.let { client -> Either.Right(listOf(client)) }
                        ?: Either.Left(AccessNotFoundException("Unable to find client for User ID $userId and Client ID $clientId"))
            }
                    ?: clientRepo.findAllEnabledClientsByUserId(userId)
                            .let { Either.Right(it) }
}

private fun createGetRoles(roleRepo: RoleRepository): (Long,Long?) -> Either<Throwable,List<Role>> {
    return fun (userId: Long, clientId: Long?): Either<Throwable,List<Role>> {
        return clientId?.let { actualClientId ->
            roleRepo.findAllByUserIdAndClientId(userId, clientId).let { Either.Right(it) }
        }
                ?: roleRepo.findAllByUserId(userId).let { Either.Right(it) }
    }
}

@Service
class AccessLoadingService(
        private val userRepo: UserRepository,
        private val clientRepo: ClientRepository,
        private val roleRepo: RoleRepository
) {
    private val getUserById = createGetUserById(userRepo)
    private val getClients = createGetClients(clientRepo)
    private val getRoles = createGetRoles(roleRepo)

    // TODO need to have a client restriction for legacy calls

    fun getAccessForUser(userId: Long): Either<Throwable, UserWithClientsAccess> =
            getAccessForUserAndClient(userId)

    fun getAccessForUserAndClient(userId: Long, clientId: Long? = null): Either<Throwable,UserWithClientsAccess> =
            either.eager {
                val user = getUserById(userId).bind()
                val clients = getClients(userId, clientId).bind()
                val roles = getRoles(userId, clientId).bind()

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