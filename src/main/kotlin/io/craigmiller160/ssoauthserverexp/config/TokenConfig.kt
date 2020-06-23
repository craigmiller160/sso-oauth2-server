package io.craigmiller160.ssoauthserverexp.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
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

    // TODO delete all of this
    lateinit var publicKey: PublicKey
    lateinit var privateKey: PrivateKey
    lateinit var keyPair: KeyPair

    @PostConstruct
    fun createKeys() {
        println("Access Exp: $accessExpSecs") // TODO delete this

        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(keySizeBits)

        keyPair = keyGen.genKeyPair()
        publicKey = keyPair.public
        privateKey = keyPair.private
    }

    fun jwkSet(): JWKSet {
        val builder = RSAKey.Builder(publicKey as RSAPublicKey)
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("oauth-jwt")
        return JWKSet(builder.build())
    }

}