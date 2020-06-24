package io.craigmiller160.authserver.controller

import io.craigmiller160.authserver.config.TokenConfig
import net.minidev.json.JSONObject
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/jwk")
class JwkController (
        private val tokenConfig: TokenConfig
) {

    @GetMapping
    fun getKey(): JSONObject {
        return tokenConfig.jwkSet().toJSONObject()
    }

}