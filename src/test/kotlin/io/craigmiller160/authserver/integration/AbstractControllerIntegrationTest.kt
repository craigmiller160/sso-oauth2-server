package io.craigmiller160.authserver.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.craigmiller160.apitestprocessor.ApiTestProcessor
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc

@AutoConfigureMockMvc
abstract class AbstractControllerIntegrationTest {

    protected lateinit var apiProcessor: ApiTestProcessor

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun apiProcessorSetup() {
        apiProcessor = ApiTestProcessor(
                mockMvc = mockMvc,
                objectMapper = objectMapper,
                isSecure = false
        )
    }

}
