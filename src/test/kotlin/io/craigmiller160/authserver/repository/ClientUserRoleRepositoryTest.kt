package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.ClientUserRole
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

@SpringBootTest
@ExtendWith(SpringExtension::class)
class ClientUserRoleRepositoryTest {
    @Autowired
    private lateinit var userRepo: UserRepository
    @Autowired
    private lateinit var clientRepo: ClientRepository
    @Autowired
    private lateinit var clientUserRoleRepo: ClientUserRoleRepository
    @Autowired
    private lateinit var roleRepo: RoleRepository
    @Autowired
    private lateinit var entityManager: EntityManager

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
    fun test_findAllByUserId() {
        TODO("Finish this")
    }
}