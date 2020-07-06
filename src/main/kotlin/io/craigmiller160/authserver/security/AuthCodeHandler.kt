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
