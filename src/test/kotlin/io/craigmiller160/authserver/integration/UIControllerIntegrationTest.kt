package io.craigmiller160.authserver.integration

import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.testutils.TestData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class UIControllerIntegrationTest : AbstractControllerIntegrationTest() {

    @Autowired
    private lateinit var clientRepo: ClientRepository

    private lateinit var client1: Client
    private lateinit var client2: Client

    @BeforeEach
    fun setup() {
        client1 = TestData.createClient()
        client2 = TestData.createClient().copy(
                clientKey = "key2"
        )

        client1 = clientRepo.save(client1)
        client2 = clientRepo.save(client2)
    }

    @AfterEach
    fun clean() {
        clientRepo.deleteAll()
    }

    @Test
    fun test_getCss_bootstrap() {
        val result = apiProcessor.call {
            request {
                path = "/ui/resources/css/bootstrap.min.css"
            }
            response {
                headers = mapOf(
                        "Content-Type" to "text/css"
                )
            }
        }

        assertTrue(result.response.contentAsString.isNotBlank())
    }

    @Test
    fun test_getCss_other() {
        apiProcessor.call {
            request {
                path = "/ui/resources/css/other.min.css"
            }
            response {
                status = 404
            }
        }
    }

    @Test
    fun test_getPage_login() {
        val result = apiProcessor.call {
            request {
                path = "/ui/login.html?client_id=${client1.clientKey}&redirect_uri=${client1.redirectUri}&response_type=code"
            }
            response {
                headers = mapOf(
                        "Content-Type" to "text/html"
                )
            }
        }

        assertTrue(result.response.contentAsString.isNotBlank())
    }

    @Test
    fun test_getPage_noAuthCode() {
        apiProcessor.call {
            request {
                path = "/ui/login.html?client_id=${client2.clientKey}&redirect_uri=${client2.redirectUri}&response_type=code"
            }
            response {
                status = 401
            }
        }
    }

    @Test
    fun test_getPage_other() {
        apiProcessor.call {
            request {
                path = "/ui/foo.html?client_id=${client1.clientKey}&redirect_uri=${client1.redirectUri}&response_type=code"
            }
            response {
                status = 404
            }
        }
    }

    @Test
    fun test_getPage_badParams() {
        apiProcessor.call {
            request {
                path = "/ui/login.html?client_id=${client1.clientKey}&redirect_uri=${client1.redirectUri}&response_type=code2"
            }
            response {
                status = 401
            }
        }
    }

}
