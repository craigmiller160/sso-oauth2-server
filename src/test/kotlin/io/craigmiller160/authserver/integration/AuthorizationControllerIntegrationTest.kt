package io.craigmiller160.authserver.integration

import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.apitestprocessor.body.Json
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.dto.access.ClientWithRolesAccess
import io.craigmiller160.authserver.dto.access.UserWithClientsAccess
import io.craigmiller160.authserver.dto.authorization.LoginTokenRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
class AuthorizationControllerIntegrationTest : AbstractControllerIntegrationTest() {
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
    testAccessToken(tokenId, accessToken)
    testRefreshToken(tokenId, refreshToken)
    testRefreshTokenInDb(tokenId, refreshToken)
  }

  private fun testRefreshTokenInDb(tokenId: String, refreshToken: String) {
    val dbRefreshToken = refreshTokenRepo.findById(tokenId).orElseThrow()
    assertThat(dbRefreshToken)
      .hasFieldOrPropertyWithValue("refreshToken", refreshToken)
      .hasFieldOrPropertyWithValue("userId", authUser.id)
  }

  private fun testRefreshToken(tokenId: String, refreshToken: String) {
    val refreshJwt = SignedJWT.parse(refreshToken)
    val refreshClaims = refreshJwt.jwtClaimsSet
    assertThat(refreshClaims.claims)
      .containsEntry("jti", tokenId)
      .containsKey("iat")
      .containsKey("exp")
      .containsKey("nbf")
  }

  private fun testAccessToken(tokenId: String, accessToken: String) {
    val accessJwt = SignedJWT.parse(accessToken)
    val accessClaims = accessJwt.jwtClaimsSet
    assertThat(accessClaims.claims)
      .containsEntry("jti", tokenId)
      .containsKey("iat")
      .containsKey("exp")
      .containsKey("nbf")

    val expectedClients =
      mapOf(
        validClientKey to
          ClientWithRolesAccess(
            clientId = authClient.id, clientName = authClient.name, roles = listOf()))

    val access = UserWithClientsAccess.fromClaims(accessClaims)
    assertThat(access)
      .hasFieldOrPropertyWithValue("userId", authUser.id)
      .hasFieldOrPropertyWithValue("email", authUser.email)
      .hasFieldOrPropertyWithValue("firstName", authUser.firstName)
      .hasFieldOrPropertyWithValue("lastName", authUser.lastName)
      .hasFieldOrPropertyWithValue("clients", expectedClients)
  }

  @Test
  fun `Valid credentials, set cookie in response to caller`() {
    TODO("Finish this")
  }

  @Test
  fun `Valid credentials, set cookie and send redirect in response to caller`() {
    TODO("Finish this")
  }

  @Test
  fun `User does not exist`() {
    TODO("Finish this")
  }

  @Test
  fun `User is disabled`() {
    TODO("Finish this")
  }

  @Test
  fun `Invalid password`() {
    TODO("Finish this")
  }
}
