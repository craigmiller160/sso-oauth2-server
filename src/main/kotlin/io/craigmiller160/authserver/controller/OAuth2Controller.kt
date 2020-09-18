/*
 *     Auth Management Service
 *     Copyright (C) 2020 Craig Miller
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.craigmiller160.authserver.controller

import io.craigmiller160.authserver.dto.AuthCodeLogin
import io.craigmiller160.authserver.dto.TokenRequest
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.exception.BadRequestException
import io.craigmiller160.authserver.exception.UnsupportedGrantTypeException
import io.craigmiller160.authserver.security.GrantType
import io.craigmiller160.authserver.service.OAuth2Service
import io.craigmiller160.authserver.utils.encodeUriParams
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/oauth")
class OAuth2Controller(
        private val oAuth2Service: OAuth2Service
) {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/token", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun token(tokenRequest: TokenRequest): TokenResponse {
        validateTokenRequest(tokenRequest)
        return when (tokenRequest.grant_type) {
            GrantType.PASSWORD -> oAuth2Service.password(tokenRequest)
            GrantType.AUTH_CODE -> oAuth2Service.authCode(tokenRequest)
            GrantType.REFRESH_TOKEN -> oAuth2Service.refresh(tokenRequest.refresh_token!!)
            else -> throw UnsupportedGrantTypeException(tokenRequest.grant_type)
        }
    }

    @PostMapping("/auth")
    fun authCodeLogin(login: AuthCodeLogin, req: HttpServletRequest, res: HttpServletResponse) {
        try {
            oAuth2Service.validateAuthCodeLogin(login)
            val authCode = oAuth2Service.authCodeLogin(login)
            val successParams = encodeUriParams(mapOf(
                    "code" to authCode,
                    "state" to login.state
            ))
            val successRedirectUrl = "${login.redirectUri}?$successParams"
            res.status = 302
            res.addHeader("Location", successRedirectUrl)
        } catch (ex: Exception) {
            log.error("Error during login", ex)
            val failParams = encodeUriParams(mapOf(
                    "response_type" to login.responseType,
                    "client_id" to login.clientId,
                    "redirect_uri" to login.redirectUri,
                    "state" to login.state,
                    "fail" to "true"
            ))
            val failRedirectUri = "${login.basePath}/ui/login?$failParams"
            res.status = 302
            res.addHeader("Location", failRedirectUri)
        }
    }

    private fun validateTokenRequest(tokenRequest: TokenRequest) {
        if (GrantType.PASSWORD == tokenRequest.grant_type && (StringUtils.isBlank(tokenRequest.username) || StringUtils.isBlank(tokenRequest.password))) {
            throw BadRequestException("Invalid token request")
        }

        if (GrantType.REFRESH_TOKEN == tokenRequest.grant_type && StringUtils.isBlank(tokenRequest.refresh_token)) {
            throw BadRequestException("Invalid token request")
        }

        if (GrantType.AUTH_CODE == tokenRequest.grant_type &&
                (StringUtils.isBlank(tokenRequest.client_id) || StringUtils.isBlank(tokenRequest.code) ||
                        StringUtils.isBlank(tokenRequest.redirect_uri))) {
            throw BadRequestException("Invalid token request")
        }
    }

}
