package io.craigmiller160.authserver.integration

import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.apitestprocessor.body.Json
import io.craigmiller160.authserver.dto.access.ClientWithRolesAccess
import io.craigmiller160.authserver.dto.access.UserWithClientsAccess
import io.craigmiller160.authserver.dto.access.fromClaims
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import io.craigmiller160.authserver.dto.tokenResponse.TokenResponse
import io.craigmiller160.authserver.security.ACCESS_TOKEN_COOKIE_NAME
import io.craigmiller160.authserver.security.REFRESH_TOKEN_COOKIE_NAME
import io.craigmiller160.authserver.security.REFRESH_TOKEN_COOKIE_PATH
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class AuthorizationControllerIntegrationTest : AbstractControllerIntegrationTest() {
  companion object {
    private val COOKIE_REGEX =
      """^(?<cookieName>.*?)=(?<cookieValue>.*?); (Path=(?<path>.*?); )?Max-Age=(?<maxAge>.*?); Expires=(?<expires>.*?); Secure; HttpOnly; SameSite=strict$""".toRegex()
  }
  @Test
  fun `Valid credentials, create and return tokens to the caller`() {
    val request = LoginTokenRequest(username = authUser.email, password = authUserPassword)
    val result =
      authApiProcessor
        .call {
          request {
            method = HttpMethod.POST
            path = "/authorization/token"
            body = Json(request)
          }
          response { status = 200 }
        }
        .convert(TokenResponse::class.java)
    val (accessToken, refreshToken, tokenId) = result
    testAccessToken(accessToken, tokenId)
    testRefreshToken(refreshToken, tokenId)
    testRefreshTokenInDb(refreshToken, tokenId)
  }

  private fun testRefreshTokenInDb(refreshToken: String, tokenId: String) {
    val dbRefreshToken = refreshTokenRepo.findById(tokenId).orElseThrow()
    assertThat(dbRefreshToken)
      .hasFieldOrPropertyWithValue("refreshToken", refreshToken)
      .hasFieldOrPropertyWithValue("userId", authUser.id)
  }

  private fun testRefreshToken(refreshToken: String, tokenId: String) {
    val refreshJwt = SignedJWT.parse(refreshToken)
    val refreshClaims = refreshJwt.jwtClaimsSet
    assertThat(refreshClaims.claims)
      .containsEntry("jti", tokenId)
      .containsKey("iat")
      .containsKey("exp")
      .containsKey("nbf")
  }

  private fun testAccessToken(accessToken: String, tokenId: String? = null): String {
    val accessJwt = SignedJWT.parse(accessToken)
    val accessClaims = accessJwt.jwtClaimsSet
    assertThat(accessClaims.claims).containsKey("iat").containsKey("exp").containsKey("nbf")
    val actualTokenId = accessClaims.jwtid
    tokenId?.let { assertThat(actualTokenId).isEqualTo(it) }

    val expectedClients =
      mapOf(
        validClientKey to
          ClientWithRolesAccess(
            clientId = authClient.id, clientName = authClient.name, roles = listOf()))

    val access = UserWithClientsAccess.fromClaims(accessClaims.claims)
    assertThat(access)
      .hasFieldOrPropertyWithValue("userId", authUser.id)
      .hasFieldOrPropertyWithValue("email", authUser.email)
      .hasFieldOrPropertyWithValue("firstName", authUser.firstName)
      .hasFieldOrPropertyWithValue("lastName", authUser.lastName)
      .hasFieldOrPropertyWithValue("clients", expectedClients)
    return actualTokenId
  }

  @Test
  fun `Valid credentials, set cookie in response to caller`() {
    val request =
      LoginTokenRequest(username = authUser.email, password = authUserPassword, cookie = true)
    val result =
      authApiProcessor.call {
        request {
          method = HttpMethod.POST
          path = "/authorization/token"
          body = Json(request)
        }
        response { status = 204 }
      }
    assertThat(result.response.contentAsString).isEmpty()
    val cookies = result.response.getHeaderValues("Set-Cookie") as List<String>
    assertThat(cookies).hasSize(2)
    validateCookies(cookies)
  }

  private fun validateCookies(cookies: List<String>) {
    val (accessCookie, refreshCookie) =
      cookies
        .partition { it.startsWith(ACCESS_TOKEN_COOKIE_NAME) }
        .let { Pair(it.first[0], it.second[0]) }
    val accessCookieGroups =
      COOKIE_REGEX.matchEntire(accessCookie)?.groups as? MatchNamedGroupCollection?
    assertThat(accessCookieGroups).isNotNull
    assertThat(accessCookieGroups!!["cookieName"]?.value).isEqualTo(ACCESS_TOKEN_COOKIE_NAME)
    assertThat(accessCookieGroups["path"]?.value).isNull()
    val tokenId = testAccessToken(accessCookieGroups["cookieValue"]?.value ?: "")

    val refreshCookieGroups =
      COOKIE_REGEX.matchEntire(refreshCookie)?.groups as? MatchNamedGroupCollection?
    assertThat(refreshCookieGroups).isNotNull
    assertThat(refreshCookieGroups!!["cookieName"]?.value).isEqualTo(REFRESH_TOKEN_COOKIE_NAME)
    assertThat(refreshCookieGroups["path"]?.value).isEqualTo(REFRESH_TOKEN_COOKIE_PATH)
    testRefreshToken(refreshCookieGroups["cookieValue"]?.value ?: "", tokenId)
    testRefreshTokenInDb(refreshCookieGroups["cookieValue"]?.value ?: "", tokenId)
  }

  @Test
  fun `Valid credentials, set cookie and send redirect in response to caller`() {
    val redirectUri = "http://somewhere/over/rainbow"
    val request =
      LoginTokenRequest(
        username = authUser.email,
        password = authUserPassword,
        cookie = true,
        redirectUri = redirectUri)
    val result =
      authApiProcessor.call {
        request {
          method = HttpMethod.POST
          path = "/authorization/token"
          body = Json(request)
        }
        response { status = 302 }
      }
    assertThat(result.response.contentAsString).isEmpty()
    val cookies = result.response.getHeaderValues("Set-Cookie") as List<String>
    assertThat(result.response.getHeaderValue("Location")).isEqualTo(redirectUri)
    assertThat(cookies).hasSize(2)
    validateCookies(cookies)
  }

  @Test
  fun `User does not exist`() {
    val request = LoginTokenRequest(username = "abc@gmail.com", password = authUserPassword)
    val result =
      authApiProcessor.call {
        request {
          method = HttpMethod.POST
          path = "/authorization/token"
          body = Json(request)
        }
        response { status = 401 }
      }
  }

  @Test
  fun `User is disabled`() {
    val request = LoginTokenRequest(username = disabledUser.email, password = authUserPassword)
    val result =
      authApiProcessor.call {
        request {
          method = HttpMethod.POST
          path = "/authorization/token"
          body = Json(request)
        }
        response { status = 401 }
      }
  }

  @Test
  fun `Invalid password`() {
    val request = LoginTokenRequest(username = authUser.email, password = "fooBar")
    val result =
      authApiProcessor.call {
        request {
          method = HttpMethod.POST
          path = "/authorization/token"
          body = Json(request)
        }
        response { status = 401 }
      }
  }
}
