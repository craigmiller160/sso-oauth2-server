package io.craigmiller160.ssoauthserverexp.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Base64
import javax.annotation.PostConstruct
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@Configuration
@ConfigurationProperties(prefix = "security.token")
class TokenConfig (
        var accessExpSecs: Int = 0,
        var refreshExpSecs: Int = 0,
        var keySizeBits: Int = 0
) {

    lateinit var publicKey: PublicKey
    lateinit var privateKey: PrivateKey

    @PostConstruct
    fun createKeys() {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(keySizeBits)

        val keyPair = keyGen.genKeyPair()
        publicKey = keyPair.public
        privateKey = keyPair.private
    }

}