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

package io.craigmiller160.authserver.security

import io.craigmiller160.authserver.config.TokenConfig
import io.craigmiller160.authserver.exception.AuthCodeException
import org.springframework.stereotype.Component
import java.security.GeneralSecurityException
import java.util.Base64
import javax.crypto.Cipher

@Component
class AuthCodeHandler (
        private val tokenConfig: TokenConfig
) {

    private val delimiter = "|"

    fun createAuthCode(clientId: Long, userId: Long, expSecs: Int): String {
        val exp = System.currentTimeMillis() + (expSecs * 1000)
        val rawToken: String = "$clientId$delimiter$userId$delimiter$exp"
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, tokenConfig.privateKey)

        val encryptedBytes = cipher.doFinal(rawToken.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun validateAuthCode(authCode: String): Pair<Long,Long> {
        val encryptedBytes = Base64.getDecoder().decode(authCode)
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, tokenConfig.publicKey)

        val decryptedBytes = try {
            cipher.doFinal(encryptedBytes)
        } catch (ex: GeneralSecurityException) {
            throw AuthCodeException("Invalid Auth Code encryption", ex)
        }

        val rawToken = String(decryptedBytes)
        val (clientId, userId, exp) = rawToken.split(delimiter)

        if (exp.toLong() < System.currentTimeMillis()) {
            throw AuthCodeException("Auth Code is expired")
        }

        return Pair(clientId.toLong(), userId.toLong())
    }

}
