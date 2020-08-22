package io.craigmiller160.authserver.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.apitestprocessor.ApiTestProcessor
import io.craigmiller160.apitestprocessor.config.AuthType
import io.craigmiller160.authserver.config.TokenConfig
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.testutils.TestData
import io.craigmiller160.date.converter.LegacyDateConverter
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.text.CharSequenceLength
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import java.util.Base64
import javax.crypto.Cipher

@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
abstract class AbstractControllerIntegrationTest {

    protected lateinit var apiProcessor: ApiTestProcessor

    @Autowired
    private lateinit var provMockMvc: MockMvc

    @Autowired
    private lateinit var provObjMapper: ObjectMapper

    @Autowired
    private lateinit var clientRepo: ClientRepository
    protected lateinit var authClient: Client

    @Autowired
    private lateinit var tokenConfig: TokenConfig

    private val bcryptEncoder = BCryptPasswordEncoder()

    protected val validClientKey = "ValidClientKey"
    protected val validClientSecret = "ValidClientSecret"
    protected val validClientName = "ValidClientName"
    protected val accessTokenTimeoutSecs = 100
    protected val refreshTokenTimeoutSecs = 1000

    private val dateConverter = LegacyDateConverter()

    @BeforeEach
    fun apiProcessorSetup() {
        apiProcessor = ApiTestProcessor {
            mockMvc = provMockMvc
            objectMapper = provObjMapper
            auth {
                type = AuthType.BASIC
                userName = validClientKey
                password = validClientSecret
                isSecure = true
            }
        }

        val encodedSecret = bcryptEncoder.encode(validClientSecret)

        authClient = TestData.createClient(accessTokenTimeoutSecs, refreshTokenTimeoutSecs).copy(
                name = validClientName,
                clientKey = validClientKey,
                clientSecret = "{bcrypt}$encodedSecret"
        )
        authClient = clientRepo.save(authClient)
    }

    @AfterEach
    fun apiProcessorCleanup() {
        clientRepo.delete(authClient)
    }

    protected fun doEncrypt(value: String): String {
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, tokenConfig.privateKey)
        val bytes = cipher.doFinal(value.toByteArray())
        return Base64.getEncoder().encodeToString(bytes)
    }

    protected fun testTokenResponse(tokenResponse: TokenResponse) {
        val (accessToken, refreshToken, tokenId) = tokenResponse
        MatcherAssert.assertThat(tokenId, CharSequenceLength.hasLength(Matchers.greaterThan(0)))
        MatcherAssert.assertThat(accessToken, CharSequenceLength.hasLength(Matchers.greaterThan(0)))
        MatcherAssert.assertThat(refreshToken, CharSequenceLength.hasLength(Matchers.greaterThan(0)))

        testAccessToken(accessToken, tokenId)
        testRefreshToken(refreshToken, tokenId)
    }

    private fun testRefreshToken(refreshToken: String, tokenId: String) {
        val refreshJwt = SignedJWT.parse(refreshToken)
        val refreshClaims = refreshJwt.jwtClaimsSet

        val expTime = dateConverter.convertDateToLocalDateTime(refreshClaims.expirationTime)
        val issueTime = dateConverter.convertDateToLocalDateTime(refreshClaims.issueTime)
        val notBeforeTime = dateConverter.convertDateToLocalDateTime(refreshClaims.notBeforeTime)

        MatcherAssert.assertThat(expTime, Matchers.equalTo(issueTime.plusSeconds(refreshTokenTimeoutSecs.toLong())))
        MatcherAssert.assertThat(expTime, Matchers.equalTo(notBeforeTime.plusSeconds(refreshTokenTimeoutSecs.toLong())))
        MatcherAssert.assertThat(refreshClaims.jwtid, Matchers.equalTo(tokenId))
        MatcherAssert.assertThat(refreshClaims.getClaim("grantType") as String, Matchers.equalTo("client_credentials"))
        MatcherAssert.assertThat(refreshClaims.getClaim("clientId") as Long, Matchers.equalTo(authClient.id))
        MatcherAssert.assertThat(refreshClaims.getClaim("userId"), Matchers.nullValue())
    }

    private fun testAccessToken(accessToken: String, tokenId: String) {
        val accessJwt = SignedJWT.parse(accessToken)
        val accessClaims = accessJwt.jwtClaimsSet

        val expTime = dateConverter.convertDateToLocalDateTime(accessClaims.expirationTime)
        val issueTime = dateConverter.convertDateToLocalDateTime(accessClaims.issueTime)
        val notBeforeTime = dateConverter.convertDateToLocalDateTime(accessClaims.notBeforeTime)

        MatcherAssert.assertThat(expTime, Matchers.equalTo(issueTime.plusSeconds(accessTokenTimeoutSecs.toLong())))
        MatcherAssert.assertThat(expTime, Matchers.equalTo(notBeforeTime.plusSeconds(accessTokenTimeoutSecs.toLong())))
        MatcherAssert.assertThat(accessClaims.jwtid, Matchers.equalTo(tokenId))
        MatcherAssert.assertThat(accessClaims.getClaim("clientKey") as String, Matchers.equalTo(validClientKey))
        MatcherAssert.assertThat(accessClaims.getClaim("clientName") as String, Matchers.equalTo(validClientName))
        MatcherAssert.assertThat(accessClaims.getStringListClaim("roles"), Matchers.equalTo(listOf()))
        MatcherAssert.assertThat(accessClaims.subject, Matchers.equalTo(validClientName))

        MatcherAssert.assertThat(accessClaims.getClaim("userEmail"), Matchers.nullValue())
        MatcherAssert.assertThat(accessClaims.getClaim("firstName"), Matchers.nullValue())
        MatcherAssert.assertThat(accessClaims.getClaim("lastName"), Matchers.nullValue())
    }

}
