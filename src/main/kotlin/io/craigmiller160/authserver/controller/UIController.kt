/*
 *     sso-oauth2-server
 *     Copyright (C) 2020 Craig Miller
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.craigmiller160.authserver.controller

import io.craigmiller160.authserver.dto.PageRequest
import io.craigmiller160.authserver.service.UIService
import javax.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/ui")
class UIController(private val uiService: UIService) {

  @GetMapping("/csrf") fun getCsrf(): ResponseEntity<Void> = ResponseEntity.noContent().build()

  @GetMapping("/resources/css/{resourceName}")
  fun getCss(@PathVariable resourceName: String, res: HttpServletResponse) {
    javaClass.classLoader.getResourceAsStream("ui/css/$resourceName.css")?.let { resourceStream ->
      res.contentType = "text/css"
      resourceStream.copyTo(res.outputStream)
    }
      ?: res.apply { status = 404 }
  }

  @GetMapping("/{pageName}")
  fun getPage(@PathVariable pageName: String, res: HttpServletResponse, pageRequest: PageRequest) {
    uiService.validateRequest(pageRequest)
    javaClass.classLoader.getResourceAsStream("ui/$pageName.html")?.let { pageStream ->
      res.contentType = "text/html"
      pageStream.copyTo(res.outputStream)
    }
      ?: res.apply { status = 404 }
  }
}
