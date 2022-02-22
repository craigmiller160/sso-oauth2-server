package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.RoleRepository
import io.craigmiller160.authserver.repository.UserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class AccessLoadingServiceTest {

    @Mock
    private lateinit var userRepo: UserRepository
    @Mock
    private lateinit var clientRepo: ClientRepository
    @Mock
    private lateinit var roleRepo: RoleRepository
    @InjectMocks
    private lateinit var accessLoadingService: AccessLoadingService

    @Test
    fun `getAccessForUser() - no user found`() {
        TODO("Finish this")
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