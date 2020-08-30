package io.craigmiller160.authserver.service

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.isA
import io.craigmiller160.authserver.dto.RefreshTokenData
import io.craigmiller160.authserver.dto.TokenRequest
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.ClientRedirectUri
import io.craigmiller160.authserver.entity.RefreshToken
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.AuthCodeException
import io.craigmiller160.authserver.exception.InvalidLoginException
import io.craigmiller160.authserver.exception.InvalidRefreshTokenException
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.RefreshTokenRepository
import io.craigmiller160.authserver.repository.RoleRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.craigmiller160.authserver.security.AuthCodeHandler
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

    @Mock
    private lateinit var authCodeHandler: AuthCodeHandler
    @Mock
    private lateinit var clientRepo: ClientRepository

    @InjectMocks
    private lateinit var oAuth2Service: OAuth2Service

    private val tokenId = "tokenId"
    private val accessToken = "AccessToken"
    private val refreshToken = "RefreshToken"
    private val password = "{bcrypt}\$2a\$10\$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe"
    private val authCode = "ABCDEFG"

    private val client = TestData.createClient()
    private val clientUserDetails = ClientUserDetails(client)
    private val user = User(
            id = 1L,
            email = "craig@gmail.com",
            firstName = "Craig",
            lastName = "Miller",
            password = password,
            enabled = true
    )
    private val role = Role(
            id = 1L,
            name = "Role",
            clientId = 1L
    )
    private val roles = listOf(role)
    private val tokenData = RefreshTokenData(
            tokenId = tokenId,
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
                .thenReturn(Pair(accessToken, tokenId))
        `when`(jwtHandler.createRefreshToken(clientUserDetails, GrantType.CLIENT_CREDENTIALS, tokenId = tokenId))
                .thenReturn(Pair(refreshToken, tokenId))

        val result = oAuth2Service.clientCredentials()
        assertEquals(TokenResponse(accessToken, refreshToken, tokenId), result)

        verify(refreshTokenRepo, times(1))
                .save(isA<RefreshToken>())
        verify(refreshTokenRepo, times(1))
                .removeClientOnlyRefresh(client.id)
    }

    @Test
    fun test_password() {
        setupSecurityContext()
        `when`(jwtHandler.createAccessToken(clientUserDetails, user, roles))
                .thenReturn(Pair(accessToken, tokenId))
        `when`(jwtHandler.createRefreshToken(clientUserDetails, GrantType.PASSWORD, user.id, tokenId))
                .thenReturn(Pair(refreshToken, tokenId))

        `when`(userRepo.findByEmailAndClientId(user.email, client.id))
                .thenReturn(user)
        `when`(roleRepo.findAllByUserIdAndClientId(user.id, client.id))
                .thenReturn(roles)

        val tokenRequest = TestData.createTokenRequest(GrantType.PASSWORD, username = user.email, password = "password")
        val result = oAuth2Service.password(tokenRequest)
        assertEquals(TokenResponse(accessToken, refreshToken, tokenId), result)

        verify(refreshTokenRepo, times(1))
                .save(isA<RefreshToken>())
        verify(refreshTokenRepo, times(1))
                .removeClientUserRefresh(client.id, user.id)
    }

    @Test
    fun test_password_noUserFound() {
        setupSecurityContext()
        val tokenRequest = TestData.createTokenRequest(GrantType.PASSWORD, username = user.email, password = "password")

        val ex = assertThrows<InvalidLoginException> { oAuth2Service.password(tokenRequest) }
        assertEquals("User does not exist for client", ex.message)
    }

    @Test
    fun test_password_wrongPassword() {
        setupSecurityContext()

        `when`(userRepo.findByEmailAndClientId(user.email, client.id))
                .thenReturn(user)

        val tokenRequest = TestData.createTokenRequest(GrantType.PASSWORD, username = user.email, password = "password2")
        val ex = assertThrows<InvalidLoginException> { oAuth2Service.password(tokenRequest) }
        assertEquals("Invalid credentials", ex.message)
    }

    @Test
    fun test_authCode() {
        setupSecurityContext()
        val request = TestData.createTokenRequest(GrantType.AUTH_CODE, clientId = client.clientKey, redirectUri = client.getRedirectUris()[0], code = authCode)

        `when`(userRepo.findByUserIdAndClientId(user.id, client.id))
                .thenReturn(user)
        `when`(authCodeHandler.validateAuthCode(authCode))
                .thenReturn(Pair(client.id, user.id))
        `when`(roleRepo.findAllByUserIdAndClientId(user.id, client.id))
                .thenReturn(roles)
        `when`(jwtHandler.createAccessToken(clientUserDetails, user, roles))
                .thenReturn(Pair(accessToken, tokenId))
        `when`(jwtHandler.createRefreshToken(clientUserDetails, GrantType.AUTH_CODE, user.id, tokenId))
                .thenReturn(Pair(refreshToken, tokenId))

        val result = oAuth2Service.authCode(request)
        assertEquals(TokenResponse(accessToken, refreshToken, tokenId), result)

        verify(refreshTokenRepo, times(1))
                .save(isA<RefreshToken>())
        verify(refreshTokenRepo, times(1))
                .removeClientUserRefresh(client.id, user.id)
    }

    @Test
    fun test_authCode_invalidClientKey() {
        setupSecurityContext()
        val request = TestData.createTokenRequest(GrantType.AUTH_CODE, clientId = "abc", redirectUri = client.getRedirectUris()[0], code = authCode)

        val ex = assertThrows<InvalidLoginException> { oAuth2Service.authCode(request) }
        assertEquals("Invalid client id", ex.message)
    }

    @Test
    fun test_authCode_invalidRedirectUri() {
        setupSecurityContext()
        val request = TestData.createTokenRequest(GrantType.AUTH_CODE, clientId = client.clientKey, redirectUri = "abc", code = authCode)

        val ex = assertThrows<InvalidLoginException> { oAuth2Service.authCode(request) }
        assertEquals("Invalid redirect uri", ex.message)
    }

    @Test
    fun test_authCode_invalidAuthCodeClientId() {
        setupSecurityContext()
        val request = TestData.createTokenRequest(GrantType.AUTH_CODE, clientId = client.clientKey, redirectUri = client.getRedirectUris()[0], code = authCode)

        `when`(authCodeHandler.validateAuthCode(authCode))
                .thenReturn(Pair(2, user.id))

        val ex = assertThrows<InvalidLoginException> { oAuth2Service.authCode(request) }
        assertEquals("Invalid auth code client", ex.message)
    }

    @Test
    fun test_authCode_invalidUser() {
        setupSecurityContext()
        val request = TestData.createTokenRequest(GrantType.AUTH_CODE, clientId = client.clientKey, redirectUri = client.getRedirectUris()[0], code = authCode)

        `when`(authCodeHandler.validateAuthCode(authCode))
                .thenReturn(Pair(client.id, user.id))

        val ex = assertThrows<InvalidLoginException> { oAuth2Service.authCode(request) }
        assertEquals("Invalid user", ex.message)
    }

    @Test
    fun test_refresh() {
        setupSecurityContext()
        `when`(jwtHandler.parseRefreshToken(refreshToken, client.id))
                .thenReturn(tokenData)

        val refreshTokenEntity = RefreshToken(tokenData.tokenId, refreshToken, client.id, user.id, LocalDateTime.now())

        `when`(refreshTokenRepo.findById(tokenData.tokenId))
                .thenReturn(Optional.of(refreshTokenEntity))
        `when`(userRepo.findByUserIdAndClientId(tokenData.userId!!, client.id))
                .thenReturn(user)
        `when`(roleRepo.findAllByUserIdAndClientId(user.id, client.id))
                .thenReturn(roles)
        `when`(jwtHandler.createAccessToken(clientUserDetails, user, roles))
                .thenReturn(Pair(accessToken, tokenId))
        `when`(jwtHandler.createRefreshToken(clientUserDetails, tokenData.grantType, user.id, tokenId))
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
                .thenReturn(Pair(accessToken, tokenId))
        `when`(jwtHandler.createRefreshToken(clientUserDetails, tokenData.grantType, tokenId = tokenId))
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
        assertEquals("Invalid Refresh User", ex.message)
    }

    @Test
    fun test_authCodeLogin() {
        val login = TestData.createAuthCodeLogin()
        `when`(clientRepo.findByClientKey(client.clientKey))
                .thenReturn(client)
        `when`(userRepo.findByEmailAndClientId(user.email, client.id))
                .thenReturn(user)
        `when`(passwordEncoder.matches("password", "{bcrypt}\$2a\$10\$HYKpEK6BFUFH99fHm5yOhuk4hn1gFErtLveeonVSHW1G7n5bUhGUe"))
                .thenReturn(true)

        `when`(authCodeHandler.createAuthCode(client.id, user.id, client.authCodeTimeoutSecs!!))
                .thenReturn(authCode)

        val result = oAuth2Service.authCodeLogin(login)
        assertEquals(authCode, result)
    }

    @Test
    fun test_authCodeLogin_badClient() {
        val login = TestData.createAuthCodeLogin()

        val ex = assertThrows<AuthCodeException> { oAuth2Service.authCodeLogin(login) }
        assertEquals("Client not supported", ex.message)
    }

    @Test
    fun test_authCodeLogin_badUser() {
        val login = TestData.createAuthCodeLogin()
        `when`(clientRepo.findByClientKey(client.clientKey))
                .thenReturn(client)

        val ex = assertThrows<AuthCodeException> { oAuth2Service.authCodeLogin(login) }
        assertEquals("User not found", ex.message)
    }

    @Test
    fun test_authCodeLogin_badPassword() {
        val login = TestData.createAuthCodeLogin()
        `when`(clientRepo.findByClientKey(client.clientKey))
                .thenReturn(client)
        `when`(userRepo.findByEmailAndClientId(user.email, client.id))
                .thenReturn(user)
        `when`(passwordEncoder.matches(any(), any()))
                .thenReturn(false)

        val ex = assertThrows<AuthCodeException> { oAuth2Service.authCodeLogin(login) }
        assertEquals("Invalid credentials", ex.message)
    }

    @Test
    fun test_validateAuthCodeLogin_success() {
        val login = TestData.createAuthCodeLogin()
        `when`(clientRepo.findByClientKey(client.clientKey))
                .thenReturn(client)
        `when`(userRepo.findByEmailAndClientId(login.username, client.id))
                .thenReturn(user)

        oAuth2Service.validateAuthCodeLogin(login)
        // No tests needed, if an exception is not thrown, then this is a success
    }

    @Test
    fun test_validateAuthCodeLogin_invalidResponseType() {
        val login = TestData.createAuthCodeLogin().copy(responseType = "abc")

        val ex = assertThrows<AuthCodeException> { oAuth2Service.validateAuthCodeLogin(login) }
        assertEquals("Invalid response type", ex.message)
    }

    @Test
    fun test_validateAuthCodeLogin_badClient() {
        val login = TestData.createAuthCodeLogin()

        val ex = assertThrows<AuthCodeException> { oAuth2Service.validateAuthCodeLogin(login) }
        assertEquals("Client not supported", ex.message)
    }

    @Test
    fun test_validateAuthCodeLogin_authCodeNotSupported() {
        val login = TestData.createAuthCodeLogin()
        `when`(clientRepo.findByClientKey(client.clientKey))
                .thenReturn(client.copy(clientRedirectUris = listOf(ClientRedirectUri(0, 0, ""))))
        `when`(userRepo.findByEmailAndClientId(login.username, client.id))
                .thenReturn(user)

        val ex = assertThrows<AuthCodeException> { oAuth2Service.validateAuthCodeLogin(login) }
        assertEquals("Client does not support Auth Code", ex.message)
    }

    @Test
    fun test_validateAuthCodeLogin_noState() {
        val login = TestData.createAuthCodeLogin().copy(state = "")

        val ex = assertThrows<AuthCodeException> { oAuth2Service.validateAuthCodeLogin(login) }
        assertEquals("No state property", ex.message)
    }

}
