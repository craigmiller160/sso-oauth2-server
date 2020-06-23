package io.craigmiller160.ssoauthserverexp.config

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.FileNotFoundException

class TokenConfigTest {

    private val accessExpSecs = 10
    private val refreshExpSecs = 20
    private val keyStoreType = "JKS"
    private val keyStorePassword = "password"
    private val keyStoreAlias = "jwt"

    private lateinit var tokenConfig: TokenConfig

    @BeforeEach
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

    @Test
    fun test_loadKeys_classpath_notFound() {
        tokenConfig.keyStorePath = "classpath:not/real.jks"
        assertThrows<FileNotFoundException> { tokenConfig.loadKeys() }
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

//    @Test(expected = FileNotFoundException::class)
//    fun test_loadKeys_file_notFound() {
//        val file = File("not/exists.jks")
//        tokenConfig.keyStorePath = file.absolutePath
//        tokenConfig.loadKeys()
//    }

}