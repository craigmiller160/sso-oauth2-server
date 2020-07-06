package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.Client
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
class ClientRepositoryTest {

    @Autowired
    private lateinit var clientRepo: ClientRepository
    private val client = TestData.createClient()

    @BeforeEach
    fun setup() {
        clientRepo.save(client)
    }

    @AfterEach
    fun clean() {
        clientRepo.deleteAll()
    }

    @Test
    fun test_findByClientKey() {
        val result = clientRepo.findByClientKey("Key")
        assertNotNull(result)
        assertEquals(client.clientSecret, result!!.clientSecret)
    }

    @Test
    fun test_findByClientKey_noResults() {
        val result = clientRepo.findByClientKey("abc")
        assertNull(result)
    }

}
