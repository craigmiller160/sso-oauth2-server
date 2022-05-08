/*
 *     sso-oauth2-server
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

package io.craigmiller160.authserver.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.apitestprocessor.ApiTestProcessor
import io.craigmiller160.apitestprocessor.config.AuthType
import io.craigmiller160.authserver.config.TokenConfig
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.ClientRedirectUri
import io.craigmiller160.authserver.entity.ClientUser
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.repository.ClientUserRepository
import io.craigmiller160.authserver.repository.RefreshTokenRepository
import io.craigmiller160.authserver.repository.UserRepository
import io.craigmiller160.authserver.testutils.TestData
import io.craigmiller160.date.converter.LegacyDateConverter
import java.util.Base64
import javax.crypto.Cipher
import org.apache.catalina.filters.RestCsrfPreventionFilter
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.text.CharSequenceLength
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.web.servlet.MockMvc

@AutoConfigureMockMvc
abstract class AbstractControllerIntegrationTest {

  protected lateinit var oauth2ApiProcessor: ApiTestProcessor
  protected lateinit var authApiProcessor: ApiTestProcessor

  @Autowired private lateinit var provMockMvc: MockMvc

  @Autowired protected lateinit var provObjMapper: ObjectMapper

  @Autowired protected lateinit var userRepo: UserRepository
  @Autowired protected lateinit var clientUserRepo: ClientUserRepository
  @Autowired protected lateinit var clientRepo: ClientRepository
  @Autowired protected lateinit var refreshTokenRepo: RefreshTokenRepository
  protected lateinit var authClient: Client
  protected lateinit var authUser: User
  private lateinit var authClientUser: ClientUser
  private lateinit var disabledClientClientUser: ClientUser
  private lateinit var disabledUserClientUser: ClientUser
  protected lateinit var authUserPassword: String
  protected lateinit var disabledClient: Client
  protected lateinit var disabledUser: User

  @Autowired private lateinit var tokenConfig: TokenConfig

  private val bcryptEncoder = BCryptPasswordEncoder()

  protected val validClientKey = "ValidClientKey"
  protected val validClientSecret = "ValidClientSecret"
  protected val validClientName = "ValidClientName"
  protected val accessTokenTimeoutSecs = 100
  protected val refreshTokenTimeoutSecs = 1000

  private val dateConverter = LegacyDateConverter()

  @MockBean private lateinit var csrfFilter: FilterRegistrationBean<RestCsrfPreventionFilter>

  @BeforeEach
  fun apiProcessorSetup() {
    oauth2ApiProcessor = ApiTestProcessor {
      mockMvc = provMockMvc
      objectMapper = provObjMapper
      auth {
        type = AuthType.BASIC
        userName = validClientKey
        password = validClientSecret
        isSecure = true
      }
    }
    authApiProcessor = ApiTestProcessor {
      mockMvc = provMockMvc
      objectMapper = provObjMapper
    }

    val encodedSecret = bcryptEncoder.encode(validClientSecret)

    authClient =
      TestData.createClient(accessTokenTimeoutSecs, refreshTokenTimeoutSecs)
        .copy(
          name = validClientName,
          clientKey = validClientKey,
          clientSecret = "{bcrypt}$encodedSecret")
    authClient = clientRepo.save(authClient)
    val clientRedirectUri =
      ClientRedirectUri(0, authClient.id, "http://somewhere.com/authcode/code")
    authClient = authClient.copy(clientRedirectUris = listOf(clientRedirectUri))
    authClient = clientRepo.save(authClient)

    authUser = TestData.createUser()
    authUser = userRepo.save(authUser)

    authClientUser = ClientUser(0, authUser.id, authClient.id)
    authUserPassword = authUser.password
    authUser =
      userRepo.save(authUser.copy(password = "{bcrypt}${bcryptEncoder.encode(authUserPassword)}"))
    authClientUser = clientUserRepo.save(authClientUser)

    disabledClient =
      TestData.createClient(accessTokenTimeoutSecs, refreshTokenTimeoutSecs)
        .copy(
          name = "DisabledClient",
          clientKey = "DisabledKey",
          clientSecret = "{bcrypt}$encodedSecret",
          enabled = false)
    disabledClient = clientRepo.save(disabledClient)

    disabledClientClientUser = ClientUser(0, authUser.id, disabledClient.id)
    disabledClientClientUser = clientUserRepo.save(disabledClientClientUser)

    disabledUser =
      TestData.createUser()
        .copy(
          email = "disabled@gmail.com",
          password = "{bcrypt}${bcryptEncoder.encode(authUserPassword)}",
          enabled = false)
    disabledUser = userRepo.save(disabledUser)

    disabledUserClientUser = ClientUser(0, disabledUser.id, authClient.id)
    disabledUserClientUser = clientUserRepo.save(disabledUserClientUser)
  }

  @AfterEach
  fun apiProcessorCleanup() {
    refreshTokenRepo.deleteAll()
    clientUserRepo.delete(authClientUser)
    clientUserRepo.delete(disabledClientClientUser)
    clientUserRepo.delete(disabledUserClientUser)
    clientRepo.delete(authClient)
    clientRepo.delete(disabledClient)
    userRepo.delete(authUser)
    userRepo.delete(disabledUser)
  }

  protected fun doEncrypt(value: String): String {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.ENCRYPT_MODE, tokenConfig.privateKey)
    val bytes = cipher.doFinal(value.toByteArray())
    return Base64.getEncoder().encodeToString(bytes)
  }

  protected fun testTokenResponse(
    tokenResponse: TokenResponse,
    grantType: String,
    isUser: Boolean = false
  ) {
    val (accessToken, refreshToken, tokenId) = tokenResponse
    MatcherAssert.assertThat(tokenId, CharSequenceLength.hasLength(Matchers.greaterThan(0)))
    MatcherAssert.assertThat(accessToken, CharSequenceLength.hasLength(Matchers.greaterThan(0)))
    MatcherAssert.assertThat(refreshToken, CharSequenceLength.hasLength(Matchers.greaterThan(0)))

    testAccessToken(accessToken, tokenId, isUser)
    testRefreshToken(refreshToken, tokenId, grantType, isUser)
  }

  private fun testRefreshToken(
    refreshToken: String,
    tokenId: String,
    grantType: String,
    isUser: Boolean
  ) {
    val refreshJwt = SignedJWT.parse(refreshToken)
    val refreshClaims = refreshJwt.jwtClaimsSet

    val expTime = dateConverter.convertDateToLocalDateTime(refreshClaims.expirationTime)
    val issueTime = dateConverter.convertDateToLocalDateTime(refreshClaims.issueTime)
    val notBeforeTime = dateConverter.convertDateToLocalDateTime(refreshClaims.notBeforeTime)

    assertThat(expTime, equalTo(issueTime.plusSeconds(refreshTokenTimeoutSecs.toLong())))
    assertThat(expTime, equalTo(notBeforeTime.plusSeconds(refreshTokenTimeoutSecs.toLong())))
    assertThat(refreshClaims.jwtid, equalTo(tokenId))
    assertThat(refreshClaims.getClaim("grantType") as String, equalTo(grantType))
    assertThat(refreshClaims.getClaim("clientId") as Long, equalTo(authClient.id))
    if (isUser) {
      assertThat(refreshClaims.getClaim("userId") as Long, equalTo(authUser.id))
    } else {
      assertThat(refreshClaims.getClaim("userId"), nullValue())
    }
  }

  // TODO this needs to be refactored for new token design
  private fun testAccessToken(accessToken: String, tokenId: String, isUser: Boolean) {
    val accessJwt = SignedJWT.parse(accessToken)
    val accessClaims = accessJwt.jwtClaimsSet

    val expTime = dateConverter.convertDateToLocalDateTime(accessClaims.expirationTime)
    val issueTime = dateConverter.convertDateToLocalDateTime(accessClaims.issueTime)
    val notBeforeTime = dateConverter.convertDateToLocalDateTime(accessClaims.notBeforeTime)

    assertThat(expTime, equalTo(issueTime.plusSeconds(accessTokenTimeoutSecs.toLong())))
    assertThat(expTime, equalTo(notBeforeTime.plusSeconds(accessTokenTimeoutSecs.toLong())))
    assertThat(accessClaims.jwtid, equalTo(tokenId))
    assertThat(accessClaims.getClaim("clientKey") as String, equalTo(validClientKey))
    assertThat(accessClaims.getClaim("clientName") as String, equalTo(validClientName))
    assertThat(accessClaims.getStringListClaim("roles"), equalTo(listOf()))
    if (isUser) {
      assertThat(accessClaims.subject, equalTo(authUser.email))
      assertThat(accessClaims.getClaim("userEmail") as String, equalTo(authUser.email))
      assertThat(accessClaims.getClaim("firstName") as String, equalTo(authUser.firstName))
      assertThat(accessClaims.getClaim("lastName") as String, equalTo(authUser.lastName))
    } else {
      assertThat(accessClaims.subject, equalTo(validClientName))
      assertThat(accessClaims.getClaim("userEmail"), nullValue())
      assertThat(accessClaims.getClaim("firstName"), nullValue())
      assertThat(accessClaims.getClaim("lastName"), nullValue())
    }
  }
}
