package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.ClientUser
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.testutils.TestData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class UserRepositoryTest {

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
        user = userRepo.save(TestData.createUser())
        client = clientRepo.save(TestData.createClient())
        clientUser = clientUserRepo.save(TestData.createClientUser(user.id, client.id))
    }

    @AfterEach
    fun clean() {
        clientRepo.deleteAll()
        clientRepo.deleteAll()
        userRepo.deleteAll()
    }

    @Test
    fun test_findByEmailAndClientId_found() {
        val result = userRepo.findByEmailAndClientId(user.email, client.id)
        assertNotNull(result)
        assertEquals(user, result)
    }

    @Test
    fun test_findByEmailAndClientId_notFound() {
        val result = userRepo.findByEmailAndClientId(user.email, 0)
        assertNull(result)
    }

}
