package io.craigmiller160.authserver.controller

import io.craigmiller160.authserver.service.UIService
import io.craigmiller160.authserver.testutils.TestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.io.PrintWriter
import java.io.StringWriter
import javax.servlet.http.HttpServletResponse

@ExtendWith(MockitoExtension::class)
class UIControllerTest {

    @Mock
    private lateinit var uiService: UIService

    @InjectMocks
    private lateinit var uiController: UIController

    @Mock
    private lateinit var res: HttpServletResponse

    private lateinit var writer: StringWriter

    @BeforeEach
    fun setup() {
        writer = StringWriter()
    }

    @Test
    fun test_getCss() {
        `when`(res.writer)
                .thenReturn(PrintWriter(writer))
        uiController.getCss("bootstrap.min.css", res)

        val contentTypeCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(res)
                .contentType = contentTypeCaptor.capture()

        assertTrue(writer.toString().isNotEmpty())
        assertEquals("text/css", contentTypeCaptor.value)
    }

    @Test
    fun test_getCss_404() {
        uiController.getCss("abc.css", res)

        val statusCaptor = ArgumentCaptor.forClass(Int::class.java)
        verify(res)
                .status = statusCaptor.capture()

        assertEquals(404, statusCaptor.value)
    }

    @Test
    fun test_getPage() {
        val request = TestData.createPageRequest()
        `when`(res.writer)
                .thenReturn(PrintWriter(writer))
        uiController.getPage("login.html", res, request)

        val contentTypeCaptor = ArgumentCaptor.forClass(String::class.java)
        verify(res)
                .contentType = contentTypeCaptor.capture()

        assertTrue(writer.toString().isNotEmpty())
        assertEquals("text/html", contentTypeCaptor.value)
    }

    @Test
    fun test_getPage_404() {
        val request = TestData.createPageRequest()
        uiController.getPage("abc.html", res, request)

        val statusCaptor = ArgumentCaptor.forClass(Int::class.java)
        verify(res)
                .status = statusCaptor.capture()

        assertEquals(404, statusCaptor.value)
    }

}
