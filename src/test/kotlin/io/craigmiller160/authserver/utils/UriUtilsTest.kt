package io.craigmiller160.authserver.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class UriUtilsTest {

    @Test
    fun test_encodeUriParams() {
        val params = mapOf("one" to "two", "three" to "four")
        val result = encodeUriParams(params)
        assertEquals("one=two&three=four", result)
    }

}
