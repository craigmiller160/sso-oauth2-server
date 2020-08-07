package io.craigmiller160.authserver.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.apitestprocessor.ApiTestProcessor
import io.craigmiller160.apitestprocessor.config.AuthType
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.testutils.TestData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc

@AutoConfigureMockMvc
abstract class AbstractControllerIntegrationTest {

    protected lateinit var apiProcessor: ApiTestProcessor

    @Autowired
    private lateinit var provMockMvc: MockMvc

    @Autowired
    private lateinit var provObjMapper: ObjectMapper

    @Autowired
    private lateinit var clientRepo: ClientRepository
    private lateinit var authClient: Client

    protected val validClientKey = "ValidClientKey"
    protected val validClientSecret = "ValidClientSecret"
    protected val validClientName = "ValidClientName"

    @BeforeEach
    fun apiProcessorSetup() {
        apiProcessor = ApiTestProcessor {
            mockMvc = provMockMvc
            objectMapper = provObjMapper
            auth {
                type = AuthType.BASIC
                userName = validClientKey
                password = validClientSecret
            }
        }

        authClient = TestData.createClient().copy(
                name = validClientName,
                clientKey = validClientKey,
                clientSecret = validClientSecret
        )
        authClient = clientRepo.save(authClient)
    }

    @AfterEach
    fun apiProcessorCleanup() {
        clientRepo.delete(authClient)
    }

}
