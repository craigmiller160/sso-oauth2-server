/*
 *     sso-oauth2-server
 *     Copyright (C) 2020 Craig Miller
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.craigmiller160.authserver.config

import org.junit.jupiter.api.Assertions.assertEquals
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
        val path = "classpath:not/real.jks"
        tokenConfig.keyStorePath = path
        val ex = assertThrows<FileNotFoundException> { tokenConfig.loadKeys() }
        assertEquals(path, ex.message)
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

    @Test
    fun test_loadKeys_file_notFound() {
        val file = File("not/exists.jks")
        tokenConfig.keyStorePath = file.absolutePath
        val ex = assertThrows<FileNotFoundException> { tokenConfig.loadKeys() }
        assertEquals(file.absolutePath, ex.message)
    }
}
