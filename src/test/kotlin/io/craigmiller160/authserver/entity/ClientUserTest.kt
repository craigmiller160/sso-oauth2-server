package io.craigmiller160.authserver.entity

import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.ClientUserRepository
import io.craigmiller160.authserver.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class ClientUserTest {

    @Autowired
    private lateinit var userRepo: UserRepository
    @Autowired
    private lateinit var clientRepo: ClientRepository
    @Autowired
    private lateinit var clientUserRepo: ClientUserRepository

    private lateinit var user: User
    private lateinit var client: Client
    private lateinit var clientUser: ClientUser

    @BeforeEach
    fun setup() {
        user = userRepo.save(User(
                id = 1,
                email = "email",
                firstName = "firstName",
                lastName = "lastName",
                password = "password",
                enabled = true
        ))
        client = clientRepo.save(Client(
                id = 1,
                name = "name",
                clientKey = "clientKey",
                clientSecret = "clientSecret",
                enabled = true,
                accessTokenTimeoutSecs = 1,
                refreshTokenTimeoutSecs = 1,
                authCodeTimeoutSecs = 1,
                clientRedirectUris = listOf()
        ))
        clientUser = clientUserRepo.save(ClientUser(
                id = 1,
                userId = user.id,
                clientId = client.id
        ))
    }

    @AfterEach
    fun cleanup() {
        clientUserRepo.deleteAll()
        clientRepo.deleteAll()
        userRepo.deleteAll()
    }

    @Test
    fun `able to lazy load joined entities`() {
        val dbClientUserOptional = clientUserRepo.findById(clientUser.id)
        assertTrue { dbClientUserOptional.isPresent }
        val dbClientUser = dbClientUserOptional.get()

        assertEquals(this.user, dbClientUser.user)
        assertEquals(this.client, dbClientUser.client)
    }

    @Test
    fun `does not cascade deletes`() {
        TODO("Finish this")
    }
}