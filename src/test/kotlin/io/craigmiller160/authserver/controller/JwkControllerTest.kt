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

package io.craigmiller160.authserver.controller

import com.nimbusds.jose.jwk.JWKSet
import io.craigmiller160.authserver.config.TokenConfig
import net.minidev.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class JwkControllerTest {

    @Mock
    private lateinit var tokenConfig: TokenConfig
    @Mock
    private lateinit var jwkSet: JWKSet

    @InjectMocks
    private lateinit var jwkController: JwkController

    @Test
    fun test_getKey() {
        val jsonObject = JSONObject()
        jsonObject.put("Hello", "World")
        `when`(tokenConfig.jwkSet())
                .thenReturn(jwkSet)
        `when`(jwkSet.toJSONObject())
                .thenReturn(jsonObject)

        val result = jwkController.getKey()
        assertEquals(jsonObject, result)
    }

}