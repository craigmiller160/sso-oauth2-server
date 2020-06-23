package io.craigmiller160.ssoauthserverexp.security

import com.nhaarman.mockito_kotlin.anyOrNull
import io.craigmiller160.ssoauthserverexp.config.TokenConfig
import io.craigmiller160.ssoauthserverexp.util.LegacyDateConverter
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
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
class JwtCreatorTest {

    @Mock
    private lateinit var tokenConfig: TokenConfig
    @Mock
    private lateinit var legacyDateConverter: LegacyDateConverter

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
    }

    @Test
    fun test_createAccessToken() {
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