package io.craigmiller160.authserver.service

import com.nhaarman.mockito_kotlin.isA
import io.craigmiller160.authserver.dto.RefreshTokenData
import io.craigmiller160.authserver.dto.TokenRequest
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.RefreshToken
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.InvalidLoginException
import io.craigmiller160.authserver.exception.InvalidRefreshTokenException
import io.craigmiller160.authserver.repository.RefreshTokenRepository
import io.craigmiller160.authserver.repository.RoleRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.craigmiller160.authserver.security.ClientUserDetails
import io.craigmiller160.authserver.security.GrantType
import io.craigmiller160.authserver.security.JwtHandler
import io.craigmiller160.authserver.testutils.TestData
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasProperty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class OAuth2ServiceTest {

    @Mock
    private lateinit var jwtHandler: JwtHandler
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

    private val client = TestData.createClient()
    private val clientUserDetails = ClientUserDetails(client)
    private val user = User(
            id = 1L,
            email = "craig@gmail.com",
            firstName = "Craig",
            lastName = "Miller",
            password = password
    )
    private val role = Role(
            id = 1L,
            name = "Role",
            clientId = 1L
    )
    private val roles = listOf(role)
    private val tokenData = RefreshTokenData(
            tokenId = "TokenId",
            grantType = "GrantType",
            clientId = client.id,
            userId = user.id
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
        `when`(jwtHandler.createAccessToken(clientUserDetails))
                .thenReturn(accessToken)
        `when`(jwtHandler.createRefreshToken(clientUserDetails, GrantType.CLIENT_CREDENTIALS))
                .thenReturn(Pair(refreshToken, "ABC"))

        val result = oAuth2Service.clientCredentials()
        assertEquals(TokenResponse(accessToken, refreshToken), result)

        verify(refreshTokenRepo, times(1))
                .save(isA<RefreshToken>())
        verify(refreshTokenRepo, times(1))
                .removeClientOnlyRefresh(client.id)
    }

    @Test
    fun test_password() {
        setupSecurityContext()
        `when`(jwtHandler.createAccessToken(clientUserDetails, user, roles))
                .thenReturn(accessToken)
        `when`(jwtHandler.createRefreshToken(clientUserDetails, GrantType.PASSWORD, user.id))
                .thenReturn(Pair(refreshToken, "ABC"))

        `when`(userRepo.findByEmailAndClientId(user.email, client.id))
                .thenReturn(user)
        `when`(roleRepo.findAllByUserIdAndClientId(user.id, client.id))
                .thenReturn(roles)

        val tokenRequest = TokenRequest("password", user.email, "password", null)
        val result = oAuth2Service.password(tokenRequest)
        assertEquals(TokenResponse(accessToken, refreshToken), result)

        verify(refreshTokenRepo, times(1))
                .save(isA<RefreshToken>())
        verify(refreshTokenRepo, times(1))
                .removeClientUserRefresh(client.id, user.id)
    }

    @Test
    fun test_password_noUserFound() {
        setupSecurityContext()
        val tokenRequest = TokenRequest("password", user.email, "password", null)

        val ex = assertThrows<InvalidLoginException> { oAuth2Service.password(tokenRequest) }
        assertEquals("User does not exist for client", ex.message)
    }

    @Test
    fun test_password_wrongPassword() {
        setupSecurityContext()

        `when`(userRepo.findByEmailAndClientId(user.email, client.id))
                .thenReturn(user)

        val tokenRequest = TokenRequest("password", user.email, "password2", null)
        val ex = assertThrows<InvalidLoginException> { oAuth2Service.password(tokenRequest) }
        assertEquals("Invalid credentials", ex.message)
    }

    @Test
    fun test_authCode() {
        val result = oAuth2Service.authCode()
        assertEquals(TokenResponse("authCode", ""), result)
    }

    @Test
    fun test_refresh() {
        setupSecurityContext()
        `when`(jwtHandler.parseRefreshToken(refreshToken, client.id))
                .thenReturn(tokenData)

        val refreshTokenEntity = RefreshToken(tokenData.tokenId, refreshToken, client.id, user.id, LocalDateTime.now())

        `when`(refreshTokenRepo.findById(tokenData.tokenId))
                .thenReturn(Optional.of(refreshTokenEntity))
        `when`(userRepo.findById(tokenData.userId!!))
                .thenReturn(Optional.of(user))
        `when`(roleRepo.findAllByUserIdAndClientId(user.id, client.id))
                .thenReturn(roles)
        `when`(jwtHandler.createAccessToken(clientUserDetails, user, roles))
                .thenReturn(accessToken)
        `when`(jwtHandler.createRefreshToken(clientUserDetails, tokenData.grantType, user.id))
                .thenReturn(Pair(refreshToken, tokenData.tokenId))

        val result = oAuth2Service.refresh(refreshToken)
        assertThat(result, allOf(
                hasProperty("accessToken", equalTo(accessToken)),
                hasProperty("refreshToken", equalTo(refreshToken))
        ))

        verify(refreshTokenRepo, times(1))
                .delete(refreshTokenEntity)
        verify(refreshTokenRepo, times(1))
                .removeClientUserRefresh(client.id, user.id)
    }

    @Test
    fun test_refresh_noUser() {
        setupSecurityContext()
        val tokenData = this.tokenData.copy(userId = null)
        `when`(jwtHandler.parseRefreshToken(refreshToken, client.id))
                .thenReturn(tokenData)

        val refreshTokenEntity = RefreshToken(tokenData.tokenId, refreshToken, client.id, null, LocalDateTime.now())

        `when`(refreshTokenRepo.findById(tokenData.tokenId))
                .thenReturn(Optional.of(refreshTokenEntity))
        `when`(jwtHandler.createAccessToken(clientUserDetails))
                .thenReturn(accessToken)
        `when`(jwtHandler.createRefreshToken(clientUserDetails, tokenData.grantType))
                .thenReturn(Pair(refreshToken, tokenData.tokenId))

        val result = oAuth2Service.refresh(refreshToken)
        assertThat(result, allOf(
                hasProperty("accessToken", equalTo(accessToken)),
                hasProperty("refreshToken", equalTo(refreshToken))
        ))

        verify(refreshTokenRepo, times(1))
                .delete(refreshTokenEntity)
        verify(refreshTokenRepo, times(1))
                .removeClientOnlyRefresh(client.id)
    }

    @Test
    fun test_refresh_noTokenInDb() {
        setupSecurityContext()
        `when`(jwtHandler.parseRefreshToken(refreshToken, client.id))
                .thenReturn(tokenData)

        val ex = assertThrows<InvalidRefreshTokenException> { oAuth2Service.refresh(refreshToken) }
        assertEquals("Refresh Token Revoked", ex.message)
    }

    @Test
    fun test_refresh_invalidUserId() {
        setupSecurityContext()
        `when`(jwtHandler.parseRefreshToken(refreshToken, client.id))
                .thenReturn(tokenData)

        val refreshTokenEntity = RefreshToken(tokenData.tokenId, refreshToken, client.id, 2L, LocalDateTime.now())

        `when`(refreshTokenRepo.findById(tokenData.tokenId))
                .thenReturn(Optional.of(refreshTokenEntity))

        val ex = assertThrows<InvalidRefreshTokenException> { oAuth2Service.refresh(refreshToken) }
        assertEquals("Invalid Refresh UserID", ex.message)
    }

}
