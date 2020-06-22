package io.craigmiller160.ssoauthserverexp.controller

import io.craigmiller160.ssoauthserverexp.service.OAuth2Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oauth")
class OAuth2Controller(
        private val oAuth2Service: OAuth2Service
) {

    @GetMapping("/token")
    fun token(): String {
        return oAuth2Service.clientCredentials()
    }

}