package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.dto.access.ClientWithRolesAccess
import io.craigmiller160.authserver.dto.access.UserWithClientsAccess
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.ClientUser
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.AccessNotFoundException
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.ClientUserRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import javax.persistence.EntityManager

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Transactional
class AccessLoadingServiceTest {

  @Autowired private lateinit var accessLoadingService: AccessLoadingService
  @Autowired
  private lateinit var userRepo: UserRepository
  @Autowired
  private lateinit var clientRepo: ClientRepository
  @Autowired
  private lateinit var clientUserRepo: ClientUserRepository
  @Autowired
  private lateinit var entityManager: EntityManager

  private fun createUser(): User = User(
          id = 1,
          email = "craig@gmail.com",
          firstName = "Craig",
          lastName = "Miller",
          password = "password",
          enabled = true
  )
  private fun createClient(): Client = Client(
          id = 1,
          name = "The Client",
          clientKey = UUID.randomUUID().toString(),
          clientSecret = UUID.randomUUID().toString(),
          enabled = true,
          accessTokenTimeoutSecs = 5,
          refreshTokenTimeoutSecs = 5,
          authCodeTimeoutSecs = 5,
          clientRedirectUris = listOf()
  )

  @Test
  fun `getAccessForUser() - no user found`() {
    val result = accessLoadingService.getAccessForUser(1L)
    result.shouldBeLeft(
      AccessNotFoundException(
        "Error getting access for User with ID: 1: Could not find User for ID: 1",
        AccessNotFoundException("Could not find User for ID: 1")))
  }

  @Test
  fun `getAccessForUser() - no clients for user`() {
    TODO("Finish this")
  }

  @Test
  fun `getAccessForUser() - single client, no roles`() {
    val user = userRepo.save(createUser())
    val client = clientRepo.save(createClient())
    val clientUser = clientUserRepo.save(ClientUser(
            id = 1,
            userId = user.id,
            clientId = client.id
    ))

    val result = accessLoadingService.getAccessForUser(user.id)
    result.shouldBeRight(UserWithClientsAccess(
            userId = user.id,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            clients = mapOf(
                    client.clientKey to ClientWithRolesAccess(
                            clientId = client.id,
                            clientName = client.name,
                            roles = listOf()
                    )
            )
    ))
  }

  @Test
  fun `getAccessForUser() - multiple clients, no roles`() {
    TODO("Finish this")
  }

  @Test
  fun `getAccessForUser() - multiple clients, one of which has roles`() {
    TODO("Finish this")
  }

  @Test
  fun `getAccessForUser() - multiple clients, all of which have roles`() {
    TODO("Finish this")
  }
}
