package io.craigmiller160.ssoauthserverexp.repository

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired
    private lateinit var refreshTokenRepo: RefreshTokenRepository

    @BeforeEach
    fun setup() {

    }

    @AfterEach
    fun clean() {
        refreshTokenRepo.deleteAll()
    }

    @Test
    fun test_removeOldTokens() {
        TODO("Finish this")
    }

}