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

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import javax.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "security.token")
class TokenConfig {

  lateinit var keyStorePath: String
  lateinit var keyStoreType: String
  lateinit var keyStorePassword: String
  lateinit var keyStoreAlias: String
  var deleteOlderThanSecs: Long = 0
  var authorization: TokenAuthorizationConfig = TokenAuthorizationConfig()

  // Not Spring properties
  lateinit var publicKey: PublicKey
  lateinit var privateKey: PrivateKey
  lateinit var keyPair: KeyPair

  @PostConstruct
  fun loadKeys() {
    val keyStore =
        evaluatePath().use { stream ->
          val store = KeyStore.getInstance(keyStoreType)
          store.load(stream, keyStorePassword.toCharArray())
          store
        }
    privateKey = keyStore.getKey(keyStoreAlias, keyStorePassword.toCharArray()) as PrivateKey
    publicKey = keyStore.getCertificate(keyStoreAlias).publicKey
    keyPair = KeyPair(publicKey, privateKey)
  }

  private fun evaluatePath(): InputStream {
    if (keyStorePath.startsWith("classpath:")) {
      val path = keyStorePath.replace(Regex("^classpath:"), "")
      return javaClass.classLoader.getResourceAsStream(path)
          ?: throw FileNotFoundException(keyStorePath)
    }

    val file = File(keyStorePath)
    if (!file.exists()) {
      throw FileNotFoundException(keyStorePath)
    }

    return file.inputStream()
  }

  fun jwkSet(): JWKSet {
    val builder =
        RSAKey.Builder(publicKey as RSAPublicKey)
            .keyUse(KeyUse.SIGNATURE)
            .algorithm(JWSAlgorithm.RS256)
            .keyID("oauth-jwt")
    return JWKSet(builder.build())
  }
}
