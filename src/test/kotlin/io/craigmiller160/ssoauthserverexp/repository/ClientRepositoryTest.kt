package io.craigmiller160.ssoauthserverexp.repository

import org.junit.Before
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest
class ClientRepositoryTest {

    @Autowired
    private lateinit var clientRepo: ClientRepository

    @Before
    fun setup() {
        TODO("Setup DB")
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