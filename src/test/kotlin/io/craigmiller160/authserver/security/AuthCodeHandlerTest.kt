package io.craigmiller160.authserver.security

import io.craigmiller160.authserver.config.TokenConfig
import io.craigmiller160.authserver.exception.AuthCodeException
import io.craigmiller160.authserver.testutils.JwtUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.security.KeyPair
import java.util.Base64
import javax.crypto.Cipher

@ExtendWith(MockitoExtension::class)
class AuthCodeHandlerTest {

    private val clientId = 1L
    private val userId = 2L
    private val expSecs = 60

    @Mock
    private lateinit var tokenConfig: TokenConfig

    @InjectMocks
    private lateinit var authCodeHandler: AuthCodeHandler

    private lateinit var keyPair: KeyPair

    @BeforeEach
    fun setup() {
        keyPair = JwtUtils.createKeyPair()
    }

    @Test
    fun test_createAuthCode_and_validateAuthCode() {
        `when`(tokenConfig.privateKey)
                .thenReturn(keyPair.private)
        `when`(tokenConfig.publicKey)
                .thenReturn(keyPair.public)

        val authCode = authCodeHandler.createAuthCode(clientId, userId, expSecs)
        assertNotNull(authCode)

        val (resultClientId, resultUserId) = authCodeHandler.validateAuthCode(authCode)
        assertEquals(clientId, resultClientId)
        assertEquals(userId, resultUserId)
    }

    @Test
    fun test_validateAuthCode_expired() {
        `when`(tokenConfig.privateKey)
                .thenReturn(keyPair.private)
        `when`(tokenConfig.publicKey)
                .thenReturn(keyPair.public)

        val authCode = authCodeHandler.createAuthCode(clientId, userId, -100)
        assertNotNull(authCode)

        val ex = assertThrows<AuthCodeException> { authCodeHandler.validateAuthCode(authCode) }
        assertEquals("Auth Code is expired", ex.message)
    }

    @Test
    fun test_validateAuthCode_invalidEncryption() {
        `when`(tokenConfig.publicKey)
                .thenReturn(keyPair.public)

        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.public)
        val rawToken = "$clientId|$userId|${System.currentTimeMillis() + 60000}"
        val encryptedBytes = cipher.doFinal(rawToken.toByteArray())
        val encrypted = Base64.getEncoder().encodeToString(encryptedBytes)

        val ex = assertThrows<AuthCodeException> { authCodeHandler.validateAuthCode(encrypted) }
        assertEquals("Invalid Auth Code encryption", ex.message)
    }

}
