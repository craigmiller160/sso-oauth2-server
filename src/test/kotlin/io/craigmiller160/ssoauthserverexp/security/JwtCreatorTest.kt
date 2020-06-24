package io.craigmiller160.ssoauthserverexp.security

import com.nhaarman.mockito_kotlin.anyOrNull
import io.craigmiller160.ssoauthserverexp.config.TokenConfig
import io.craigmiller160.ssoauthserverexp.entity.Client
import io.craigmiller160.ssoauthserverexp.util.LegacyDateConverter
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.security.KeyPairGenerator
import java.util.Base64
import java.util.Date

@ExtendWith(MockitoExtension::class)
class JwtCreatorTest {

    @Mock
    private lateinit var tokenConfig: TokenConfig
    @Mock
    private lateinit var legacyDateConverter: LegacyDateConverter
    @Mock
    private lateinit var securityContext: SecurityContext
    @Mock
    private lateinit var authentication: Authentication

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

    @InjectMocks
    private lateinit var jwtCreator: JwtCreator

    private val accessExpSecs = 10
    private val refreshExpSecs = 20
    private val expectedHeader = """{"alg":"RS256"}"""

    @BeforeEach
    fun setup() {
        val keyPairGen = KeyPairGenerator.getInstance("RSA")
        keyPairGen.initialize(2048)

        val keyPair = keyPairGen.genKeyPair()
        `when`(tokenConfig.privateKey)
                .thenReturn(keyPair.private)
        `when`(legacyDateConverter.convertLocalDateTimeToDate(anyOrNull()))
                .thenReturn(Date())

        SecurityContextHolder.setContext(securityContext)
    }

    @AfterEach
    fun clean() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun test_createAccessToken() {
        `when`(securityContext.authentication)
                .thenReturn(authentication)
        `when`(authentication.principal)
                .thenReturn(ClientUserDetails(client))

        `when`(tokenConfig.accessExpSecs)
                .thenReturn(accessExpSecs)
        val token = jwtCreator.createAccessToken()
        val parts = token.split(".")
        val header = String(Base64.getDecoder().decode(parts[0]))
        val body = String(Base64.getDecoder().decode(parts[1]))
        assertEquals(expectedHeader, header)
        val jsonObject = JSONObject(body)
        assertThat(jsonObject.getLong("nbf"), notNullValue())
        assertThat(jsonObject.getLong("iat"), notNullValue())
        assertThat(jsonObject.getString("jti"), notNullValue())
        assertThat(jsonObject.getLong("exp"), notNullValue())
        assertThat(jsonObject.getString("clientKey"), equalTo(client.clientKey))
    }

    @Test
    fun test_createRefreshToken() {
        `when`(tokenConfig.refreshExpSecs)
                .thenReturn(refreshExpSecs)
        val token = jwtCreator.createRefreshToken()
        val parts = token.split(".")
        val header = String(Base64.getDecoder().decode(parts[0]))
        val body = String(Base64.getDecoder().decode(parts[1]))
        assertEquals(expectedHeader, header)
        val jsonObject = JSONObject(body)
        assertThat(jsonObject.getLong("nbf"), notNullValue())
        assertThat(jsonObject.getLong("iat"), notNullValue())
        assertThat(jsonObject.getString("jti"), notNullValue())
        assertThat(jsonObject.getLong("exp"), notNullValue())
    }

}