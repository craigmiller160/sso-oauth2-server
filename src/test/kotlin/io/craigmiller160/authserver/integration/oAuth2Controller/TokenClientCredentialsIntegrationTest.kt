package io.craigmiller160.authserver.integration.oAuth2Controller

import com.nimbusds.jwt.SignedJWT
import io.craigmiller160.apitestprocessor.body.formOf
import io.craigmiller160.authserver.dto.TokenResponse
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.integration.AbstractControllerIntegrationTest
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.testutils.TestData
import io.craigmiller160.date.converter.LegacyDateConverter
import org.exparity.hamcrest.date.DateMatchers.after
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.nullValue
import org.hamcrest.core.AllOf.allOf
import org.hamcrest.text.CharSequenceLength.hasLength
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpMethod
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class TokenClientCredentialsIntegrationTest : AbstractControllerIntegrationTest() {

    @Autowired
    private lateinit var clientRepo: ClientRepository

    private lateinit var client1: Client

    private val dateConverter = LegacyDateConverter()

    @BeforeEach
    fun setup() {
        client1 = TestData.createClient()
        client1 = clientRepo.save(client1)
    }

    @AfterEach
    fun clean() {
        clientRepo.deleteAll()
    }

    @Test
    fun `token() - client_credentials grant invalid client header`() {
        TODO("Finish this")
    }

    @Test
    fun `token() - client_credentials grant success`() {
        val tokenResponse = apiProcessor.call {
            request {
                path = "/oauth/token"
                method = HttpMethod.POST
                body = formOf("grant_type" to "client_credentials")
            }
        }.convert(TokenResponse::class.java)

        testTokenResponse(tokenResponse)
    }

    private fun testTokenResponse(tokenResponse: TokenResponse) {
        val (accessToken, refreshToken, tokenId) = tokenResponse
        assertThat(tokenId, hasLength(greaterThan(0)))
        assertThat(accessToken, hasLength(greaterThan(0)))
        assertThat(refreshToken, hasLength(greaterThan(0)))

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
        assertThat(accessClaims.subject, equalTo(validClientName))

        assertThat(accessClaims.getClaim("userEmail"), nullValue())
        assertThat(accessClaims.getClaim("firstName"), nullValue())
        assertThat(accessClaims.getClaim("lastName"), nullValue())
    }

}
