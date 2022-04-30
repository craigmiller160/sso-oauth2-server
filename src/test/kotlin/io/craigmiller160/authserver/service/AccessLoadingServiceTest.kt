package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.exception.AccessNotFoundException
import io.kotest.assertions.arrow.core.shouldBeLeft
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Transactional
class AccessLoadingServiceTest {

    @Autowired
    private lateinit var accessLoadingService: AccessLoadingService

    @Test
    fun `getAccessForUser() - no user found`() {
        val result = accessLoadingService.getAccessForUser(1L)
        result.shouldBeLeft(
                AccessNotFoundException("Error getting access for User with ID: 1", AccessNotFoundException("Could not find User for ID: 1"))
        )
    }

    @Test
    fun `getAccessForUser() - single client, no roles`() {
        TODO("Finish this")
    }

    @Test
    fun `getAccessForUser() - multiple clients, no roles`() {
        TODO("Finish this")
    }

    @Test
    fun `getAccessForUser() - multiple clients, one of which has roles`() {
        TODO("Finish this")
    }

    @Test
    fun `getAccessForUser() - multiple clients, all of which have roles`() {
        TODO("Finish this")
    }

}