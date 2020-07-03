package io.craigmiller160.authserver.controller

import io.craigmiller160.authserver.dto.Error
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.servlet.http.HttpServletRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.HttpRequestMethodNotSupportedException

@RestControllerAdvice
class ErrorControllerAdvice {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(AccessDeniedException::class)
    fun accessDeniedException(req: HttpServletRequest, ex: AccessDeniedException): ResponseEntity<Error> {
        log.error("", ex)
        val status = 403
        val error = Error(
                status = status,
                error = "Access Denied",
                message = ex.message ?: "",
                path = req.requestURI
        )
        return ResponseEntity
                .status(status)
                .body(error)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun methodNotSupportedException(req: HttpServletRequest, ex: HttpRequestMethodNotSupportedException): ResponseEntity<Error> {
        log.error("", ex)
        val status = 405
        val error = Error(
                status = status,
                error = "Method Not Allowed",
                message = ex.message ?: "",
                path = req.requestURI
        )
        return ResponseEntity
                .status(status)
                .body(error)
    }

    @ExceptionHandler(Exception::class)
    fun exception(req: HttpServletRequest, ex: Exception): ResponseEntity<Error> {
        log.error("", ex)
        val annotation = ex.javaClass.getAnnotation(ResponseStatus::class.java)
        val status = annotation?.code?.value() ?: 500
        val error = Error(
                status = status,
                error = annotation?.code?.reasonPhrase ?: "Internal Server Error",
                message = "${annotation?.reason ?: "Error"} - ${ex.message}",
                path = req.requestURI
        )
        return ResponseEntity
                .status(status)
                .body(error)
    }

}
