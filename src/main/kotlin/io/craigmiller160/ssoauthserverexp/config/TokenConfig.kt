package io.craigmiller160.ssoauthserverexp.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
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

    lateinit var keyString: String
    lateinit var secretKey: SecretKey

    @PostConstruct
    fun createKey() {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(keySizeBits)
        this.secretKey = keyGen.generateKey()
        this.keyString = Base64.getEncoder().encodeToString(secretKey.encoded)
    }

}