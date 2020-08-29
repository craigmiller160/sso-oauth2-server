package io.craigmiller160.authserver.controller

import io.craigmiller160.authserver.dto.PageRequest
import io.craigmiller160.authserver.service.UIService
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/ui")
class UIController (
        private val uiService: UIService
) {

    @GetMapping("/resources/css/{resourceName}")
    fun getCss(@PathVariable resourceName: String, res: HttpServletResponse) {
        javaClass.classLoader.getResourceAsStream("ui/css/$resourceName.css") // TODO update tests
                ?.let { resourceStream ->
                    res.contentType = "text/css"
                    IOUtils.copy(resourceStream, res.writer, StandardCharsets.UTF_8)
                }
                ?: res.apply {
                    status = 404
                }
    }

    @GetMapping("/{pageName}")
    fun getPage(@PathVariable pageName: String, res: HttpServletResponse, pageRequest: PageRequest) {
        uiService.validateRequest(pageRequest)
        javaClass.classLoader.getResourceAsStream("ui/$pageName.html") // TODO update tests
                ?.let { pageStream ->
                    res.contentType = "text/html"
                    IOUtils.copy(pageStream, res.writer, StandardCharsets.UTF_8)
                }
                ?: res.apply {
                    status = 404
                }
    }

}
