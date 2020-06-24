package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.RefreshToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDateTime

@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired
    private lateinit var refreshTokenRepo: RefreshTokenRepository

    private lateinit var token1: RefreshToken
    private lateinit var token2: RefreshToken
    private lateinit var token3: RefreshToken

    @BeforeEach
    fun setup() {
        token1 = RefreshToken("1", "ABC", LocalDateTime.of(2020, 1, 1, 1, 1))
        token2 = RefreshToken("2", "DEF", LocalDateTime.of(2020, 2, 2, 2, 2))
        token3 = RefreshToken("3", "GHI", LocalDateTime.now())

        token1 = refreshTokenRepo.save(token1)
        token2 = refreshTokenRepo.save(token2)
        token3 = refreshTokenRepo.save(token3)
    }

    @AfterEach
    fun clean() {
        refreshTokenRepo.deleteAll()
    }

    @Test
    fun test_removeOldTokens() {
        val maxTimestamp = LocalDateTime.now().minusDays(1)
        val result = refreshTokenRepo.removeOldTokens(maxTimestamp)
        assertEquals(2, result)

        val remaining = refreshTokenRepo.findAll()
        assertEquals(1, remaining.size)
        assertEquals(token3, remaining[0])
    }

}