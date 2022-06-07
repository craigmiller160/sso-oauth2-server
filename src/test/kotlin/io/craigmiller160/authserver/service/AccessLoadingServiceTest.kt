package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.dto.access.ClientWithRolesAccess
import io.craigmiller160.authserver.dto.access.UserWithClientsAccess
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.ClientUser
import io.craigmiller160.authserver.entity.ClientUserRole
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.AccessNotFoundException
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.ClientUserRepository
import io.craigmiller160.authserver.repository.ClientUserRoleRepository
import io.craigmiller160.authserver.repository.RoleRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Transactional
class AccessLoadingServiceTest {

  @Autowired private lateinit var accessLoadingService: AccessLoadingService
  @Autowired private lateinit var userRepo: UserRepository
  @Autowired private lateinit var clientRepo: ClientRepository
  @Autowired private lateinit var clientUserRepo: ClientUserRepository
  @Autowired private lateinit var roleRepo: RoleRepository
  @Autowired private lateinit var clientUserRoleRepo: ClientUserRoleRepository

  private fun createUser(): User =
      User(
          id = 0,
          email = "craig@gmail.com",
          firstName = "Craig",
          lastName = "Miller",
          password = "password",
          enabled = true)
  private fun createClient(): Client =
      Client(
          id = 0,
          name = "The Client",
          clientKey = UUID.randomUUID().toString(),
          clientSecret = UUID.randomUUID().toString(),
          enabled = true,
          accessTokenTimeoutSecs = 5,
          refreshTokenTimeoutSecs = 5,
          authCodeTimeoutSecs = 5,
          clientRedirectUris = listOf())

  @Test
  fun `getAccessForUser() - no user found`() {
    val result = accessLoadingService.getAccessForUser(1L)
    result.shouldBeLeft(AccessNotFoundException("Could not find User for ID: 1"))
  }

  @Test
  fun `getAccessForUser() - no clients for user`() {
    val user = userRepo.save(createUser())
    val result = accessLoadingService.getAccessForUser(user.id)
    result.shouldBeRight(
        UserWithClientsAccess(
            userId = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            clients = mapOf()))
  }

  @Test
  fun `getAccessForUser() - single client, no roles`() {
    val user = userRepo.save(createUser())
    val client = clientRepo.save(createClient())
    clientUserRepo.save(ClientUser(id = 0, userId = user.id, clientId = client.id))

    val result = accessLoadingService.getAccessForUser(user.id)
    result.shouldBeRight(
        UserWithClientsAccess(
            userId = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            clients =
                mapOf(
                    client.clientKey to
                        ClientWithRolesAccess(
                            clientId = client.id, clientName = client.name, roles = listOf()))))
  }

  @Test
  fun `getAccessForUser() - multiple clients, no roles`() {
    val user = userRepo.save(createUser())
    val client1 = clientRepo.save(createClient())
    val client2 = clientRepo.save(createClient())
    clientUserRepo.save(ClientUser(id = 0, userId = user.id, clientId = client1.id))
    clientUserRepo.save(ClientUser(id = 0, userId = user.id, clientId = client2.id))

    val result = accessLoadingService.getAccessForUser(user.id)
    result.shouldBeRight(
        UserWithClientsAccess(
            userId = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            clients =
                mapOf(
                    client1.clientKey to
                        ClientWithRolesAccess(
                            clientId = client1.id, clientName = client1.name, roles = listOf()),
                    client2.clientKey to
                        ClientWithRolesAccess(
                            clientId = client2.id, clientName = client2.name, roles = listOf()))))
  }

  @Test
  fun `getAccessForUser() - multiple clients, one of which has roles`() {
    val user = userRepo.save(createUser())
    val client1 = clientRepo.save(createClient())
    val client2 = clientRepo.save(createClient())
    clientUserRepo.save(ClientUser(id = 0, userId = user.id, clientId = client1.id))
    clientUserRepo.save(ClientUser(id = 0, userId = user.id, clientId = client2.id))

    val role = roleRepo.save(Role(id = 0, clientId = client1.id, name = "TheRole"))
    clientUserRoleRepo.save(
        ClientUserRole(id = 0, clientId = client1.id, userId = user.id, roleId = role.id))

    val result = accessLoadingService.getAccessForUser(user.id)
    result.shouldBeRight(
        UserWithClientsAccess(
            userId = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            clients =
                mapOf(
                    client1.clientKey to
                        ClientWithRolesAccess(
                            clientId = client1.id,
                            clientName = client1.name,
                            roles = listOf(role.name)),
                    client2.clientKey to
                        ClientWithRolesAccess(
                            clientId = client2.id, clientName = client2.name, roles = listOf()))))
  }

  @Test
  fun `getAccessForUser() - multiple clients, all of which have roles`() {
    val user = userRepo.save(createUser())
    val client1 = clientRepo.save(createClient())
    val client2 = clientRepo.save(createClient())
    clientUserRepo.save(ClientUser(id = 0, userId = user.id, clientId = client1.id))
    clientUserRepo.save(ClientUser(id = 0, userId = user.id, clientId = client2.id))

    val role1 = roleRepo.save(Role(id = 0, clientId = client1.id, name = "TheRole"))
    clientUserRoleRepo.save(
        ClientUserRole(id = 0, clientId = client1.id, userId = user.id, roleId = role1.id))

    val role2 = roleRepo.save(Role(id = 0, clientId = client2.id, name = "TheRole2"))
    clientUserRoleRepo.save(
        ClientUserRole(id = 0, clientId = client2.id, userId = user.id, roleId = role2.id))

    val result = accessLoadingService.getAccessForUser(user.id)
    result.shouldBeRight(
        UserWithClientsAccess(
            userId = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            clients =
                mapOf(
                    client1.clientKey to
                        ClientWithRolesAccess(
                            clientId = client1.id,
                            clientName = client1.name,
                            roles = listOf(role1.name)),
                    client2.clientKey to
                        ClientWithRolesAccess(
                            clientId = client2.id,
                            clientName = client2.name,
                            roles = listOf(role2.name)))))
  }
}
