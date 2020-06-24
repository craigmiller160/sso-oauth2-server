package io.craigmiller160.ssoauthserverexp.controller

import io.craigmiller160.ssoauthserverexp.exception.UnsupportedGrantTypeException
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasProperty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import javax.servlet.http.HttpServletRequest

@ExtendWith(MockitoExtension::class)
class ErrorControllerAdviceTest {

    @Mock
    private lateinit var req: HttpServletRequest

    private val errorControllerAdvice = ErrorControllerAdvice()

    @Test
    fun test_exception_withAnnotation() {
        `when`(req.requestURI).thenReturn("uri")
        val ex = UnsupportedGrantTypeException("message")

        val error = errorControllerAdvice.exception(req, ex)
        assertThat(error, allOf(
                hasProperty("status", equalTo(400)),
                hasProperty("error", equalTo("Bad Request")),
                hasProperty("message", equalTo("Unsupported OAuth2 Grant Type - message")),
                hasProperty("timestamp", notNullValue()),
                hasProperty("path", equalTo("uri"))
        ))
    }

    @Test
    fun test_exception() {
        `when`(req.requestURI).thenReturn("uri")
        val ex = Exception("message")

        val error = errorControllerAdvice.exception(req, ex)
        assertThat(error, allOf(
                hasProperty("status", equalTo(500)),
                hasProperty("error", equalTo("Internal Server Error")),
                hasProperty("message", equalTo("Error - message")),
                hasProperty("timestamp", notNullValue()),
                hasProperty("path", equalTo("uri"))
        ))
    }

    @Test
    fun test_accessDeniedException() {
        TODO("Finish this")
    }

}