package io.craigmiller160.ssoauthserverexp.config

import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileNotFoundException

@RunWith(MockitoJUnitRunner::class)
class TokenConfigTest {

    private val accessExpSecs = 10
    private val refreshExpSecs = 20
    private val keyStoreType = "JKS"
    private val keyStorePassword = "password"
    private val keyStoreAlias = "jwt"

    private lateinit var tokenConfig: TokenConfig

    @Before
    fun setup() {
        tokenConfig = TokenConfig(
                accessExpSecs,
                refreshExpSecs,
                "",
                keyStoreType,
                keyStorePassword,
                keyStoreAlias
        )
    }

    @Test
    fun test_loadKeys_classpath() {
        tokenConfig.keyStorePath = "classpath:keys/jwt.jks"
        tokenConfig.loadKeys()
        assertNotNull(tokenConfig.publicKey)
        assertNotNull(tokenConfig.privateKey)
        assertNotNull(tokenConfig.keyPair)
    }

    @Test(expected = FileNotFoundException::class)
    fun test_loadKeys_classpath_notFound() {
        tokenConfig.keyStorePath = "classpath:not/real.jks"
        tokenConfig.loadKeys()
    }

    @Test
    fun test_loadKeys_file() {
        val keystore = File("src/main/resources/keys/jwt.jks")
        tokenConfig.keyStorePath = keystore.absolutePath
        tokenConfig.loadKeys()
        assertNotNull(tokenConfig.publicKey)
        assertNotNull(tokenConfig.privateKey)
        assertNotNull(tokenConfig.keyPair)
    }

    @Test(expected = FileNotFoundException::class)
    fun test_loadKeys_file_notFound() {
        val file = File("not/exists.jks")
        tokenConfig.keyStorePath = file.absolutePath
        tokenConfig.loadKeys()
    }

}