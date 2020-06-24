package io.craigmiller160.authserver.security

import com.nhaarman.mockito_kotlin.anyOrNull
import io.craigmiller160.authserver.config.TokenConfig
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.util.LegacyDateConverter
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.security.KeyPairGenerator
import java.util.Base64
import java.util.Date

@ExtendWith(MockitoExtension::class)
class JwtHandlerTest {

    @Mock
    private lateinit var tokenConfig: TokenConfig
    @Mock
    private lateinit var legacyDateConverter: LegacyDateConverter

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
            id = 1L,
            email = "craig@gmail.com",
            password = "password",
            firstName = "Craig",
            lastName = "Miller"
    )

    @InjectMocks
    private lateinit var jwtHandler: JwtHandler

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
    }

    @Test
    fun test_createAccessToken_clientOnly() {
        `when`(tokenConfig.accessExpSecs)
                .thenReturn(accessExpSecs)

        val token = jwtHandler.createAccessToken(clientUserDetails)
        val parts = token.split(".")
        val header = String(Base64.getDecoder().decode(parts[0]))
        val body = String(Base64.getDecoder().decode(parts[1]))
        assertEquals(expectedHeader, header)

        val jsonObject = JSONObject(body)
        assertEquals(8, jsonObject.length())
        assertThat(jsonObject.getLong("nbf"), notNullValue())
        assertThat(jsonObject.getLong("iat"), notNullValue())
        assertThat(jsonObject.getString("jti"), notNullValue())
        assertThat(jsonObject.getLong("exp"), notNullValue())
        assertThat(jsonObject.getString("clientKey"), equalTo(client.clientKey))
        assertThat(jsonObject.getString("sub"), equalTo(client.name))
        assertThat(jsonObject.getString("clientName"), equalTo(client.name))
        assertEquals(0, jsonObject.getJSONArray("roles").length())
    }

    @Test
    fun test_createAccessToken_clientAndUser() {
        `when`(tokenConfig.accessExpSecs)
                .thenReturn(accessExpSecs)

        val token = jwtHandler.createAccessToken(clientUserDetails, user)
        val parts = token.split(".")
        val header = String(Base64.getDecoder().decode(parts[0]))
        val body = String(Base64.getDecoder().decode(parts[1]))
        assertEquals(expectedHeader, header)

        val jsonObject = JSONObject(body)
        assertEquals(9, jsonObject.length())
        assertThat(jsonObject.getLong("nbf"), notNullValue())
        assertThat(jsonObject.getLong("iat"), notNullValue())
        assertThat(jsonObject.getString("jti"), notNullValue())
        assertThat(jsonObject.getLong("exp"), notNullValue())
        assertThat(jsonObject.getString("clientKey"), equalTo(client.clientKey))
        assertThat(jsonObject.getString("sub"), equalTo(user.email))
        assertThat(jsonObject.getString("userEmail"), equalTo(user.email))
        assertThat(jsonObject.getString("clientName"), equalTo(client.name))
        assertEquals(0, jsonObject.getJSONArray("roles").length())
    }

    @Test
    fun test_createAccessToken_clientUserAndRoles() {
        `when`(tokenConfig.accessExpSecs)
                .thenReturn(accessExpSecs)

        val role = Role(1L, "Role1", 1L)
        val roles = listOf(role)

        val token = jwtHandler.createAccessToken(clientUserDetails, user, roles)
        val parts = token.split(".")
        val header = String(Base64.getDecoder().decode(parts[0]))
        val body = String(Base64.getDecoder().decode(parts[1]))
        assertEquals(expectedHeader, header)

        val jsonObject = JSONObject(body)
        assertEquals(9, jsonObject.length())
        assertThat(jsonObject.getLong("nbf"), notNullValue())
        assertThat(jsonObject.getLong("iat"), notNullValue())
        assertThat(jsonObject.getString("jti"), notNullValue())
        assertThat(jsonObject.getLong("exp"), notNullValue())
        assertThat(jsonObject.getString("clientKey"), equalTo(client.clientKey))
        assertThat(jsonObject.getString("sub"), equalTo(user.email))
        assertThat(jsonObject.getString("userEmail"), equalTo(user.email))
        assertThat(jsonObject.getString("clientName"), equalTo(client.name))

        val rolesArray = jsonObject.getJSONArray("roles")
        assertEquals(1, rolesArray.length())
        assertEquals(role.name, rolesArray.getString(0))
    }

    @Test
    fun test_createRefreshToken() {
        `when`(tokenConfig.refreshExpSecs)
                .thenReturn(refreshExpSecs)
        val (token, tokenId) = jwtHandler.createRefreshToken("password", 1L, 1L)
        assertNotNull(tokenId)

        val parts = token.split(".")
        val header = String(Base64.getDecoder().decode(parts[0]))
        val body = String(Base64.getDecoder().decode(parts[1]))
        assertEquals(expectedHeader, header)
        val jsonObject = JSONObject(body)
        assertEquals(7, jsonObject.length())
        assertThat(jsonObject.getLong("nbf"), notNullValue())
        assertThat(jsonObject.getLong("iat"), notNullValue())
        assertThat(jsonObject.getString("jti"), equalTo(tokenId))
        assertThat(jsonObject.getLong("exp"), notNullValue())
        assertThat(jsonObject.getString("grantType"), equalTo("password"))
        assertThat(jsonObject.getLong("clientId"), equalTo(1L))
        assertThat(jsonObject.getLong("userId"), equalTo(1L))
    }

    @Test
    fun test_createRefreshToken_noUser() {
        `when`(tokenConfig.refreshExpSecs)
                .thenReturn(refreshExpSecs)
        val (token, tokenId) = jwtHandler.createRefreshToken("password", 1L)
        assertNotNull(tokenId)

        val parts = token.split(".")
        val header = String(Base64.getDecoder().decode(parts[0]))
        val body = String(Base64.getDecoder().decode(parts[1]))
        assertEquals(expectedHeader, header)
        val jsonObject = JSONObject(body)
        assertEquals(6, jsonObject.length())
        assertThat(jsonObject.getLong("nbf"), notNullValue())
        assertThat(jsonObject.getLong("iat"), notNullValue())
        assertThat(jsonObject.getString("jti"), equalTo(tokenId))
        assertThat(jsonObject.getLong("exp"), notNullValue())
        assertThat(jsonObject.getString("grantType"), equalTo("password"))
        assertThat(jsonObject.getLong("clientId"), equalTo(1L))
    }

    @Test
    fun test_parseRefreshToken() {
        TODO("Finish this")
    }

    @Test
    fun test_parseRefreshToken_withUser() {
        TODO("Finish this")
    }

    @Test
    fun test_parseRefreshToken_badSignature() {
        TODO("Finish this")
    }

    @Test
    fun test_parseRefreshToken_expired() {
        TODO("Finish this")
    }

    @Test
    fun test_parseRefreshToken_badClientId() {
        TODO("Finish this")
    }

}