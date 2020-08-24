package io.craigmiller160.authserver.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Paths
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import javax.annotation.PostConstruct

@Configuration
@ConfigurationProperties(prefix = "security.token")
class TokenConfig (
        var keyStorePath: String = "",
        var keyStoreType: String = "",
        var keyStorePassword: String = "",
        var keyStoreAlias: String = "",
        var deleteOlderThanSecs: Long = 0
) {

    lateinit var publicKey: PublicKey
    lateinit var privateKey: PrivateKey
    lateinit var keyPair: KeyPair

    @PostConstruct
    fun loadKeys() {
        val keyStore = evaluatePath().use { stream ->
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
        val builder = RSAKey.Builder(publicKey as RSAPublicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("oauth-jwt")
        return JWKSet(builder.build())
    }

}
