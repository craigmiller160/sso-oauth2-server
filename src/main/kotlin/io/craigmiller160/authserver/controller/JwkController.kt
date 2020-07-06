package io.craigmiller160.authserver.controller

import io.craigmiller160.authserver.config.TokenConfig
import net.minidev.json.JSONObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/jwk")
class JwkController (
        private val tokenConfig: TokenConfig
) {

    @GetMapping
    fun getKey(): ResponseEntity<JSONObject> {
        val jsonObject = tokenConfig.jwkSet().toJSONObject()
        return ResponseEntity.ok(jsonObject)
    }

}
