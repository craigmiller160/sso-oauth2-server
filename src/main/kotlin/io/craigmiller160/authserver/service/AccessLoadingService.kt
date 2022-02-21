package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.exception.AccessNotFoundException
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.ClientUserRepository
import io.craigmiller160.authserver.repository.ClientUserRoleRepository
import io.craigmiller160.authserver.repository.RoleRepository
import io.craigmiller160.authserver.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class AccessLoadingService(
        private val userRepo: UserRepository,
        private val clientRepo: ClientRepository,
        private val clientUserRepo: ClientUserRepository,
        private val clientUserRoleRepo: ClientUserRoleRepository,
        private val roleRepo: RoleRepository
) {

    // TODO factor in enabled check

    // TODO customize exceptions

    // TODO very inefficient
    // TODO consider what the access level should be?
    fun getAccessForUser(userId: Long): UserWithClientsAccess {
        /*
         * 1) Get all ClientUsers for User
         * 2) Get all ClientUserRoles for User
         * 3) Get all Clients for User
         * 4) Get all Roles for Client & User
         */
        val user = userRepo.findEnabledUserById(userId)
                ?: throw AccessNotFoundException("No user for ID: $userId")
        val clientUsers = clientUserRepo.findAllByUserId(userId)
        val clientIds = clientUsers.map { it.id }
        val clients = clientRepo.findAllEnabledClientsByIds(clientIds)

        val clientUserRoles = clientUserRoleRepo.findAllByUserId(userId)
        val roleIds = clientUserRoles.map { it.roleId }.toSet()
        val roles = roleRepo.findAllByIdIn(roleIds)

        val userClientsMap = clients.associate { client ->
            val clientRoles = roles.filter { role -> role.clientId == client.id }
            val clientWithRolesAccess = ClientWithRolesAccess(
                    clientId = client.id,
                    clientName = client.name,
                    roles = clientRoles.map { it.name }
            )
            client.clientKey to clientWithRolesAccess
        }

        return UserWithClientsAccess(
                userId = userId,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                clients = userClientsMap
        )
    }

    // TODO move to other files
    data class UserWithClientsAccess(
            val userId: Long,
            val email: String,
            val firstName: String,
            val lastName: String,
            val clients: Map<String,ClientWithRolesAccess>
    )

    // TODO move to other files
    data class ClientWithRolesAccess(
            val clientId: Long,
            val clientName: String,
            val roles: List<String>
    )

}