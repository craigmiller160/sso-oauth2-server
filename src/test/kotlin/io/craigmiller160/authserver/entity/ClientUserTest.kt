package io.craigmiller160.authserver.entity

import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.ClientUserRepository
import io.craigmiller160.authserver.repository.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class ClientUserTest {

    @Autowired
    private lateinit var userRepo: UserRepository
    @Autowired
    private lateinit var clientRepo: ClientRepository
    @Autowired
    private lateinit var clientUserRepo: ClientUserRepository

    @Test
    fun `able to lazy load joined entities`() {
        TODO("Finish this")
    }

    @Test
    fun `does not cascade deletes`() {
        TODO("Finish this")
    }
}