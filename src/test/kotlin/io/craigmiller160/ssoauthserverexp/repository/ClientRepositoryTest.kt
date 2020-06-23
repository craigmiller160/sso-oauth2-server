package io.craigmiller160.ssoauthserverexp.repository

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class ClientRepositoryTest {

    @Autowired
    private lateinit var clientRepo: ClientRepository

    @BeforeEach
    fun setup() {
        println(clientRepo) // TODO delete this
    }

    @Test
    fun test_findByClientKey() {
        TODO("Finish this")
    }

    @Test
    fun test_findByClientKey_noResults() {
        TODO("Finish this")
    }

}