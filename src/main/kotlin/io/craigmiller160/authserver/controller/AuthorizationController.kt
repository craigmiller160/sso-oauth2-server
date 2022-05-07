package io.craigmiller160.authserver.controller

import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/authorization")
class AuthorizationController {
  @PostMapping("/token")
  fun token(@RequestBody request: LoginTokenRequest): ResponseEntity<*> {
    TODO("Finish this")
  }
}
