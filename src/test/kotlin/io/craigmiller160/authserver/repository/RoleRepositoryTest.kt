package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.ClientUser
import io.craigmiller160.authserver.entity.ClientUserRole
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private lateinit var userRepo: UserRepository
    @Autowired
    private lateinit var clientRepo: ClientRepository
    @Autowired
    private lateinit var roleRepo: RoleRepository
    @Autowired
    private lateinit var clientUserRoleRepo: ClientUserRoleRepository
    @Autowired
    private lateinit var clientUserRepo: ClientUserRepository

    private lateinit var user: User
    private lateinit var client: Client
    private lateinit var clientUser: ClientUser
    private lateinit var role1: Role
    private lateinit var role2: Role
    private lateinit var clientUserRole1: ClientUserRole
    private lateinit var clientUserRole2: ClientUserRole

    @BeforeEach
    fun setup() {
        user = User(
                id = 0,
                email = "craig@gmail.com",
                firstName = "Craig",
                lastName = "Miller",
                password = "password"
        )
        user = userRepo.save(user)

        client = Client(
                id = 0,
                name = "Client",
                clientKey = "Key",
                clientSecret = "Secret",
                enabled = true,
                allowClientCredentials = false,
                allowPassword = false,
                allowAuthCode = false,
                accessTokenTimeoutSecs = 0,
                refreshTokenTimeoutSecs = 0
        )
        client = clientRepo.save(client)

        clientUser = ClientUser(
                id = 0,
                userId = user.id,
                clientId = client.id
        )
        clientUser = clientUserRepo.save(clientUser)

        role1 = Role(
                id = 0,
                name = "Role1",
                clientId = client.id
        )
        role1 = roleRepo.save(role1)

        role2 = Role(
                id = 0,
                name = "Role2",
                clientId = client.id
        )
        role2 = roleRepo.save(role2)

        clientUserRole1 = ClientUserRole(
                id = 0,
                clientId = client.id,
                userId = user.id,
                roleId = role1.id
        )
        clientUserRole1 = clientUserRoleRepo.save(clientUserRole1)

        clientUserRole2 = ClientUserRole(
                id = 0,
                clientId = client.id,
                userId = user.id,
                roleId = role2.id
        )
        clientUserRole2 = clientUserRoleRepo.save(clientUserRole2)
    }

    @AfterEach
    fun clean() {
        clientUserRoleRepo.deleteAll()
        roleRepo.deleteAll()
        clientRepo.deleteAll()
        userRepo.deleteAll()
    }

    @Test
    fun test_findAllByUserIdAndClientId() {
        val results = roleRepo.findAllByUserIdAndClientId(user.id, client.id)
        assertEquals(2, results.size)
        assertTrue(results.contains(role1))
        assertTrue(results.contains(role2))
    }

}