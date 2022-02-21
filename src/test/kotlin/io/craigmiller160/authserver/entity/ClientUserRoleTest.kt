package io.craigmiller160.authserver.entity

import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.ClientUserRoleRepository
import io.craigmiller160.authserver.repository.RoleRepository
import io.craigmiller160.authserver.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class ClientUserRoleTest {
    @Autowired
    private lateinit var userRepo: UserRepository
    @Autowired
    private lateinit var clientRepo: ClientRepository
    @Autowired
    private lateinit var clientUserRoleRepo: ClientUserRoleRepository
    @Autowired
    private lateinit var roleRepo: RoleRepository

    private lateinit var user: User
    private lateinit var client: Client
    private lateinit var role: Role
    private lateinit var clientUserRole: ClientUserRole

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
        role = roleRepo.save(Role(
                id = 1,
                name = "role",
                clientId = client.id
        ))
        clientUserRole = clientUserRoleRepo.save(ClientUserRole(
                id = 1,
                clientId = client.id,
                userId = user.id,
                roleId = role.id
        ))
    }

    @AfterEach
    fun cleanup() {
        clientUserRoleRepo.deleteAll()
        roleRepo.deleteAll()
        clientRepo.deleteAll()
        userRepo.deleteAll()
    }

    @Test
    fun `able to lazy load joined entities`() {
        val dbClientUserRoleOptional = clientUserRoleRepo.findById(clientUserRole.id)
        Assertions.assertTrue { dbClientUserRoleOptional.isPresent }
        val dbClientUserRole = dbClientUserRoleOptional.get()

        assertEquals(this.user.id, dbClientUserRole.user.id)
        assertEquals(this.client.id, dbClientUserRole.client.id)
        assertEquals(this.role.id, dbClientUserRole.role.id)
    }

    @Test
    fun `does not cascade deletes`() {
        clientUserRoleRepo.deleteById(clientUserRole.id)
        assertEquals(0, clientUserRoleRepo.count())
        assertEquals(1, roleRepo.count())
        assertEquals(1, userRepo.count())
        assertEquals(1, clientRepo.count())
    }
}