package io.craigmiller160.authserver.controller

import io.craigmiller160.authserver.service.UIService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class UIControllerTest {

    @Mock
    private lateinit var uiService: UIService

    @InjectMocks
    private lateinit var uiController: UIController

    @Test
    fun test_getCss() {
        TODO("Finish this")
    }

    @Test
    fun test_getCss_404() {
        TODO("Finish this")
    }

    @Test
    fun test_getPage() {
        TODO("Finish this")
    }

    @Test
    fun test_getPage_404() {
        TODO("Finish this")
    }

}
