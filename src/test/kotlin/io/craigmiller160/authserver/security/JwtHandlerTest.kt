package io.craigmiller160.authserver.security

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.authserver.config.TokenConfig
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.exception.InvalidRefreshTokenException
import io.craigmiller160.authserver.util.LegacyDateConverter
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.time.LocalDateTime
import java.util.Base64
import java.util.Date

@ExtendWith(MockitoExtension::class)
class JwtHandlerTest {

    @Mock
    private lateinit var tokenConfig: TokenConfig
    @Spy
    private val legacyDateConverter = LegacyDateConverter()

    private val client = Client(
            id = 1L,
            name = "Name",
            clientKey = "Key",
            clientSecret = "Secret",
            enabled = true,
            allowClientCredentials = true,
            allowAuthCode = true,
            allowPassword = true,
            accessTokenTimeoutSecs = 200,
            refreshTokenTimeoutSecs = 200
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
    private val tokenId = "ABCDEFG"
    private lateinit var keyPair: KeyPair

    @BeforeEach
    fun setup() {
        val keyPairGen = KeyPairGenerator.getInstance("RSA")
        keyPairGen.initialize(2048)

        keyPair = keyPairGen.genKeyPair()
        `when`(tokenConfig.privateKey)
                .thenReturn(keyPair.private)
    }

    @Test
    fun test_createAccessToken_clientOnly() {
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
        val (token, tokenId) = jwtHandler.createRefreshToken(clientUserDetails, "password", 1L)
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
        val (token, tokenId) = jwtHandler.createRefreshToken(clientUserDetails, "password")
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

    private fun createJwt(withUser: Boolean, exp: Int, pair: KeyPair = keyPair): String {
        `when`(tokenConfig.publicKey)
                .thenReturn(pair.public)

        val grantType = if (withUser) "password" else "client_credentials"
        var claimBuilder = JWTClaimsSet.Builder()
                .claim("clientId", client.id)
                .claim("grantType", grantType)
                .expirationTime(generateExp(exp))
                .jwtID(tokenId)

        if (withUser) {
            claimBuilder = claimBuilder.claim("userId", user.id)
        }

        val claims = claimBuilder.build()
        val header = JWSHeader.Builder(JWSAlgorithm.RS256)
                .build()
        val jwt = SignedJWT(header, claims)
        val signer = RSASSASigner(tokenConfig.privateKey)

        jwt.sign(signer)
        return jwt.serialize()
    }

    private fun generateExp(expSecs: Int): Date {
        val now = LocalDateTime.now()
        val exp = now.plusSeconds(expSecs.toLong())
        return legacyDateConverter.convertLocalDateTimeToDate(exp)
    }

    @Test
    fun test_parseRefreshToken() {
        val token = createJwt(false, 1000)

        val data = jwtHandler.parseRefreshToken(token, client.id)

        assertEquals("client_credentials", data.grantType)
        assertEquals(client.id, data.clientId)
        assertEquals(tokenId, data.tokenId)
        assertNull(data.userId)
    }

    @Test
    fun test_parseRefreshToken_withUser() {
        val token = createJwt(true, 1000)

        val data = jwtHandler.parseRefreshToken(token, client.id)

        assertEquals("password", data.grantType)
        assertEquals(client.id, data.clientId)
        assertEquals(user.id, data.userId)
        assertEquals(tokenId, data.tokenId)
    }

    @Test
    fun test_parseRefreshToken_badSignature() {
        val keyPairGen = KeyPairGenerator.getInstance("RSA")
        val keyPair = keyPairGen.genKeyPair()

        val token = createJwt(true, 1000, keyPair)

        val ex = assertThrows<InvalidRefreshTokenException> { jwtHandler.parseRefreshToken(token, client.id) }
        assertEquals("Bad Signature", ex.message)
    }

    @Test
    fun test_parseRefreshToken_expired() {
        val token = createJwt(true, -1000)

        val ex = assertThrows<InvalidRefreshTokenException> { jwtHandler.parseRefreshToken(token, client.id) }
        assertEquals("Expired", ex.message)
    }

    @Test
    fun test_parseRefreshToken_badClientId() {
        val token = createJwt(true, 1000)

        val ex = assertThrows<InvalidRefreshTokenException> { jwtHandler.parseRefreshToken(token, 20) }
        assertEquals("Invalid Client ID", ex.message)
    }

}