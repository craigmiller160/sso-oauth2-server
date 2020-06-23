package io.craigmiller160.ssoauthserverexp.service

import io.craigmiller160.ssoauthserverexp.repository.ClientRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ClientUserDetailsServiceTest {

    @Mock
    private lateinit var clientRepo: ClientRepository

    @Test
    fun test_loadUserByUsername() {
        TODO("Finish this")
    }

    @Test
    fun test_loadUserByUsername_noClientKey() {
        TODO("Finish this")
    }

    @Test
    fun test_loadUserByUsername_userNotFound() {
        TODO("Finish this")
    }

}