package io.craigmiller160.ssoauthserverexp.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

@Configuration
@ConfigurationProperties(prefix = "security.token.key-store")
class TokenKeyStoreConfig (
        var path: String = "",
        var type: String = "",
        var password: String = "",
        var alias: String = ""
) {

    @PostConstruct
    fun loadKeys() {
        println("Path: $path") // TODO delete this
    }

}