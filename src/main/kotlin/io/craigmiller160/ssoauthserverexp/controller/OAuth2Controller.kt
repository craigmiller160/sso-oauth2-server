package io.craigmiller160.ssoauthserverexp.controller

import io.craigmiller160.ssoauthserverexp.dto.TokenRequest
import io.craigmiller160.ssoauthserverexp.dto.TokenResponse
import io.craigmiller160.ssoauthserverexp.exception.BadRequestException
import io.craigmiller160.ssoauthserverexp.exception.UnsupportedGrantTypeException
import io.craigmiller160.ssoauthserverexp.security.GrantType
import io.craigmiller160.ssoauthserverexp.service.OAuth2Service
import org.apache.commons.lang3.StringUtils
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
    fun token(tokenRequest: TokenRequest): TokenResponse {
        validateTokenRequest(tokenRequest)
        return when (tokenRequest.grant_type) {
            GrantType.CLIENT_CREDENTIALS -> oAuth2Service.clientCredentials()
            GrantType.PASSWORD -> oAuth2Service.password(tokenRequest)
            GrantType.AUTH_CODE -> oAuth2Service.authCode()
            else -> throw UnsupportedGrantTypeException(tokenRequest.grant_type)
        }
    }

    private fun validateTokenRequest(tokenRequest: TokenRequest) {
        if (GrantType.PASSWORD == tokenRequest.grant_type && (StringUtils.isBlank(tokenRequest.username) || StringUtils.isBlank(tokenRequest.password))) {
            throw BadRequestException("Invalid token request")
        }
    }

}