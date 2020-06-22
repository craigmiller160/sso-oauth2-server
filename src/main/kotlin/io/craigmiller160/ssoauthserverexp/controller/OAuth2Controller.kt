package io.craigmiller160.ssoauthserverexp.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oauth")
class OAuth2Controller {

    @GetMapping("/token")
    fun hello(): String {
        return "Hello World"
    }

}