package io.craigmiller160.ssoauthserverexp.controller

import io.craigmiller160.ssoauthserverexp.dto.TokenRequest
import io.craigmiller160.ssoauthserverexp.security.GrantTypes
import io.craigmiller160.ssoauthserverexp.service.OAuth2Service
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oauth")
class OAuth2Controller(
        private val oAuth2Service: OAuth2Service
) {

    @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun token(tokenRequest: TokenRequest): String {
        return when (tokenRequest.grant_type) {
            GrantTypes.CLIENT_CREDENTIALS -> oAuth2Service.clientCredentials()
            GrantTypes.PASSWORD -> oAuth2Service.password()
            GrantTypes.AUTH_CODE -> oAuth2Service.authCode()
            else -> throw Exception("Unsupported grant type") // TODO assign a better exception type
        }
    }

}