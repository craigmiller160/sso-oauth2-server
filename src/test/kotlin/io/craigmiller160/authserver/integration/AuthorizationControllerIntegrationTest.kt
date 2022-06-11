package io.craigmiller160.authserver.integration

import arrow.core.getOrHandle
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.apitestprocessor.body.Json
import io.craigmiller160.authserver.dto.access.ClientWithRolesAccess
import io.craigmiller160.authserver.dto.access.UserWithClientsAccess
import io.craigmiller160.authserver.dto.access.fromClaims
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import io.craigmiller160.authserver.dto.authorization.TokenRefreshRequest
import io.craigmiller160.authserver.dto.tokenResponse.TokenResponse
import io.craigmiller160.authserver.entity.RefreshToken
import io.craigmiller160.authserver.security.ACCESS_TOKEN_COOKIE_NAME
import io.craigmiller160.authserver.security.AuthorizationJwtHandler
import io.craigmiller160.authserver.security.REFRESH_TOKEN_COOKIE_NAME
import io.craigmiller160.authserver.security.REFRESH_TOKEN_COOKIE_PATH
import io.craigmiller160.date.converter.LegacyDateConverter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class AuthorizationControllerIntegrationTest : AbstractControllerIntegrationTest() {
  private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  @Autowired private lateinit var jwtHandler: AuthorizationJwtHandler
  @Autowired private lateinit var objectMapper: ObjectMapper
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

    val expiration =
        LegacyDateConverter()
            .convertDateToZonedDateTime(refreshClaims.expirationTime, ZoneId.systemDefault())
    assertThat(FORMATTER.format(expiration))
        .isEqualTo(FORMATTER.format(ZonedDateTime.now())) // TODO fix in the end

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
    val expiration =
        LegacyDateConverter()
            .convertDateToZonedDateTime(accessClaims.expirationTime, ZoneId.systemDefault())
    assertThat(FORMATTER.format(expiration))
        .isEqualTo(FORMATTER.format(ZonedDateTime.now())) // TODO fix in the end
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
    val mockResponse =
        authApiProcessor.call {
          request {
            method = HttpMethod.POST
            path = "/authorization/token"
            body = Json(request)
          }
          response { status = 200 }
        }
    @Suppress("UNCHECKED_CAST")
    val cookies = mockResponse.response.getHeaderValues("Set-Cookie") as List<String>
    assertThat(cookies).hasSize(2)
    validateCookies(cookies)

    val result =
        objectMapper.readValue(mockResponse.response.contentAsString, TokenResponse::class.java)
    val (accessToken, refreshToken, tokenId) = result
    testAccessToken(accessToken, tokenId)
    testRefreshToken(refreshToken, tokenId)
    testRefreshTokenInDb(refreshToken, tokenId)
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
    val mockResponse =
        authApiProcessor.call {
          request {
            method = HttpMethod.POST
            path = "/authorization/token"
            body = Json(request)
          }
          response { status = 302 }
        }
    @Suppress("UNCHECKED_CAST")
    val cookies = mockResponse.response.getHeaderValues("Set-Cookie") as List<String>
    assertThat(mockResponse.response.getHeaderValue("Location")).isEqualTo(redirectUri)
    assertThat(cookies).hasSize(2)
    validateCookies(cookies)

    val result =
        objectMapper.readValue(mockResponse.response.contentAsString, TokenResponse::class.java)
    val (accessToken, refreshToken, tokenId) = result
    testAccessToken(accessToken, tokenId)
    testRefreshToken(refreshToken, tokenId)
    testRefreshTokenInDb(refreshToken, tokenId)
  }

  @Test
  fun `User does not exist`() {
    val request = LoginTokenRequest(username = "abc@gmail.com", password = authUserPassword)
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
  fun `valid refresh token`() {
    val tokenId = UUID.randomUUID().toString()
    val refreshToken = jwtHandler.createRefreshToken(tokenId).getOrHandle { throw it }
    val refreshTokenEntity =
        RefreshToken(
            refreshToken = refreshToken,
            id = tokenId,
            userId = authUser.id,
            clientId = null,
            timestamp = ZonedDateTime.now())
    refreshTokenRepo.save(refreshTokenEntity)
    val request = TokenRefreshRequest(refreshToken)
    val result =
        authApiProcessor
            .call {
              request {
                method = HttpMethod.POST
                path = "/authorization/refresh"
                body = Json(request)
              }
            }
            .convert(TokenResponse::class.java)
    val (resultAccessToken, resultRefreshToken, resultTokenId) = result
    assertEquals(tokenId, resultTokenId)
    testAccessToken(resultAccessToken, resultTokenId)
    testRefreshToken(resultRefreshToken, resultTokenId)
    testRefreshTokenInDb(resultRefreshToken, resultTokenId)
  }

  @Test
  fun `valid refresh token, set cookies`() {
    val tokenId = UUID.randomUUID().toString()
    val refreshToken = jwtHandler.createRefreshToken(tokenId).getOrHandle { throw it }
    val refreshTokenEntity =
        RefreshToken(
            refreshToken = refreshToken,
            id = tokenId,
            userId = authUser.id,
            clientId = null,
            timestamp = ZonedDateTime.now())
    refreshTokenRepo.save(refreshTokenEntity)
    val request = TokenRefreshRequest(refreshToken, true)
    val mockResponse =
        authApiProcessor.call {
          request {
            method = HttpMethod.POST
            path = "/authorization/refresh"
            body = Json(request)
          }
        }
    @Suppress("UNCHECKED_CAST")
    val cookies = mockResponse.response.getHeaderValues("Set-Cookie") as List<String>
    assertThat(cookies).hasSize(2)
    validateCookies(cookies)

    val result =
        objectMapper.readValue(mockResponse.response.contentAsString, TokenResponse::class.java)
    val (resultAccessToken, resultRefreshToken, resultTokenId) = result
    assertEquals(tokenId, resultTokenId)
    testAccessToken(resultAccessToken, resultTokenId)
    testRefreshToken(resultRefreshToken, resultTokenId)
    testRefreshTokenInDb(resultRefreshToken, resultTokenId)
  }

  @Test
  fun `expired refresh token`() {
    val tokenId = UUID.randomUUID().toString()
    val refreshToken = jwtHandler.createRefreshToken(tokenId, -1080).getOrHandle { throw it }
    val refreshTokenEntity =
        RefreshToken(
            refreshToken = refreshToken,
            id = tokenId,
            userId = authUser.id,
            clientId = null,
            timestamp = ZonedDateTime.now())
    refreshTokenRepo.save(refreshTokenEntity)
    val request = TokenRefreshRequest(refreshToken)
    val result =
        authApiProcessor.call {
          request {
            method = HttpMethod.POST
            path = "/authorization/refresh"
            body = Json(request)
          }
          response { status = 401 }
        }
  }

  @Test
  fun `revoked refresh token`() {
    TODO("Finish this")
  }
}
