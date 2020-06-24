package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.repository.ClientRepository
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.userdetails.UsernameNotFoundException

@ExtendWith(MockitoExtension::class)
class ClientUserDetailsServiceTest {

    @Mock
    private lateinit var clientRepo: ClientRepository
    private val client = Client(10, "GHI", "ABC", "DEF", true, false, false, false)

    @InjectMocks
    private lateinit var clientUserDetailsService: ClientUserDetailsService

    @Test
    fun test_loadUserByUsername() {
        val clientKey = "ABC"
        `when`(clientRepo.findByClientKey(clientKey))
                .thenReturn(client)

        val result = clientUserDetailsService.loadUserByUsername(clientKey)
        assertThat(result, allOf(
                hasProperty("enabled", equalTo(client.enabled)),
                hasProperty("username", equalTo(client.clientKey)),
                hasProperty("password", equalTo(client.clientSecret))
        ))
    }

    @Test
    fun test_loadUserByUsername_noClientKey() {
        val ex = assertThrows<UsernameNotFoundException> { clientUserDetailsService.loadUserByUsername(null) }
        assertEquals("No Client Key to lookup", ex.message)
    }

    @Test
    fun test_loadUserByUsername_userNotFound() {
        val ex = assertThrows<UsernameNotFoundException> { clientUserDetailsService.loadUserByUsername("ABC") }
        assertEquals("No client found for Client Key ABC", ex.message)
    }

}