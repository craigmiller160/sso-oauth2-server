package io.craigmiller160.ssoauthserverexp.config

import org.junit.Before
import org.junit.Test


class TokenConfigTest {

    private val accessExpSecs = 10
    private val refreshExpSecs = 20
    private val keyStoreType = "type"
    private val keyStorePassword = "pass"
    private val keyStoreAlias = "alias"

    private lateinit var tokenConfig: TokenConfig

    @Before
    fun setup() {
        tokenConfig = TokenConfig()
    }

    @Test
    fun test_loadKeys_classpath() {
        TODO("Finish this")
    }

    @Test
    fun test_loadKeys_classpath_notFound() {
        TODO("Finish this")
    }

    @Test
    fun test_loadKeys_file() {
        TODO("Finish this")
    }

    @Test
    fun test_loadKeys_file_notFound() {
        TODO("Finish this")
    }

}