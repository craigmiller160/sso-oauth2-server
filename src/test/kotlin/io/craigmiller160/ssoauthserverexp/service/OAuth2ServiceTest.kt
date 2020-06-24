package io.craigmiller160.ssoauthserverexp.service

import com.nhaarman.mockito_kotlin.isA
import io.craigmiller160.ssoauthserverexp.dto.TokenRequest
import io.craigmiller160.ssoauthserverexp.dto.TokenResponse
import io.craigmiller160.ssoauthserverexp.entity.Client
import io.craigmiller160.ssoauthserverexp.entity.RefreshToken
import io.craigmiller160.ssoauthserverexp.entity.User
import io.craigmiller160.ssoauthserverexp.repository.RefreshTokenRepository
import io.craigmiller160.ssoauthserverexp.repository.RoleRepository
import io.craigmiller160.ssoauthserverexp.repository.UserRepository
import io.craigmiller160.ssoauthserverexp.security.ClientUserDetails
import io.craigmiller160.ssoauthserverexp.security.JwtCreator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class OAuth2ServiceTest {

    @Mock
    private lateinit var jwtCreator: JwtCreator
    @Mock
    private lateinit var refreshTokenRepo: RefreshTokenRepository
    @Mock
    private lateinit var userRepo: UserRepository
    @Mock
    private lateinit var roleRepo: RoleRepository
    @Mock
    private lateinit var securityContext: SecurityContext
    @Mock
    private lateinit var authentication: Authentication
    @Spy
    private val passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    @InjectMocks
    private lateinit var oAuth2Service: OAuth2Service

    private val accessToken = "AccessToken"
    private val refreshToken = "RefreshToken"
    private val password = "{bcrypt}\$2a\$10\$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe"

    private val client = Client(
            id = 1L,
            name = "Name",
            clientKey = "Key",
            clientSecret = "Secret",
            enabled = true,
            allowClientCredentials = true,
            allowAuthCode = true,
            allowPassword = true
    )
    private val clientUserDetails = ClientUserDetails(client)
    private val user = User(
            id = 0,
            email = "craig@gmail.com",
            firstName = "Craig",
            lastName = "Miller",
            password = password
    )

    private fun setupSecurityContext() {
        `when`(securityContext.authentication)
                .thenReturn(authentication)
        `when`(authentication.principal)
                .thenReturn(clientUserDetails)
    }

    @BeforeEach
    fun setup() {
        SecurityContextHolder.setContext(securityContext)
    }

    @AfterEach
    fun clean() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun test_clientCredentials() {
        setupSecurityContext()
        `when`(jwtCreator.createAccessToken(clientUserDetails))
                .thenReturn(accessToken)
        `when`(jwtCreator.createRefreshToken())
                .thenReturn(refreshToken)

        val result = oAuth2Service.clientCredentials()
        assertEquals(TokenResponse(accessToken, refreshToken), result)

        verify(refreshTokenRepo, times(1))
                .save(isA<RefreshToken>())
    }

    @Test
    fun test_password() {
        setupSecurityContext()
        `when`(jwtCreator.createAccessToken(clientUserDetails, user))
                .thenReturn(accessToken)
        `when`(jwtCreator.createRefreshToken())
                .thenReturn(refreshToken)

        `when`(userRepo.findByEmailAndClientId(user.email, client.id))
                .thenReturn(user)

        val tokenRequest = TokenRequest("password", user.email, "password", null)
        val result = oAuth2Service.password(tokenRequest)
        assertEquals(TokenResponse(accessToken, refreshToken), result)

        verify(refreshTokenRepo, times(1))
                .save(isA<RefreshToken>())
    }

    @Test
    fun test_password_noUserFound() {
        TODO("Finish this")
    }

    @Test
    fun test_password_wrongPassword() {
        TODO("Finish this")
    }

    @Test
    fun test_authCode() {
        val result = oAuth2Service.authCode()
        assertEquals(TokenResponse("authCode", ""), result)
    }

}