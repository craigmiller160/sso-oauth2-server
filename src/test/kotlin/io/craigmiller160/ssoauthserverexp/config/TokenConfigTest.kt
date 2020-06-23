package io.craigmiller160.ssoauthserverexp.config

import org.junit.jupiter.api.Test

class TokenConfigTest {

    private val accessExpSecs = 10
    private val refreshExpSecs = 20
    private val keyStorePath = "keyStorePath"
    private val keyStoreType = "type"
    private val keyStorePassword = "pass"
    private val keyStoreAlias = "alias"

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

}