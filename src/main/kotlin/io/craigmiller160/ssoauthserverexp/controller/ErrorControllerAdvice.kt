package io.craigmiller160.ssoauthserverexp.controller

import io.craigmiller160.ssoauthserverexp.dto.Error
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.servlet.http.HttpServletRequest

@RestControllerAdvice
class ErrorControllerAdvice {

    @ExceptionHandler(Exception::class)
    fun exception(req: HttpServletRequest, ex: Exception): Error {
        val annotation = ex.javaClass.getAnnotation(ResponseStatus::class.java)
        return Error(
                status = annotation?.code?.value() ?: 500,
                error = annotation?.code?.reasonPhrase ?: "Internal Server Error",
                message = "${annotation?.reason ?: "Error"} - ${ex.message}",
                path = req.requestURI
        )
    }

}