package io.craigmiller160.ssoauthserverexp.service

import com.nhaarman.mockito_kotlin.isA
import io.craigmiller160.ssoauthserverexp.config.TokenConfig
import io.craigmiller160.ssoauthserverexp.repository.RefreshTokenRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class RefreshTokenCleanupServiceTest {

    @Mock
    private lateinit var refreshTokenRepo: RefreshTokenRepository
    @Mock
    private lateinit var tokenConfig: TokenConfig

    @InjectMocks
    private lateinit var refreshTokenCleanupService: RefreshTokenCleanupService

    @Test
    fun test_cleanupRefreshTokens() {
        `when`(tokenConfig.deleteOlderThanSecs)
                .thenReturn(1000)
        refreshTokenCleanupService.cleanupRefreshTokens()

        verify(refreshTokenRepo, times(1))
                .removeOldTokens(isA())
    }

}