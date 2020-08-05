package io.craigmiller160.authserver.integration

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class UIControllerIntegrationTest : AbstractControllerIntegrationTest() {

    @Test
    fun test_getCss_bootstrap() {
        TODO("Finish this")
    }

    @Test
    fun test_getCss_other() {
        TODO("Finish this")
    }

    @Test
    fun test_getPage_login() {
        TODO("Finish this")
    }

    @Test
    fun test_getPage_other() {
       TODO("Finish this")
    }

    @Test
    fun test_getPage_badParams() {
        TODO("Finish this")
    }

}
