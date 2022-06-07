package io.craigmiller160.authserver.exception

import io.craigmiller160.authserver.dto.ErrorResponse
import io.craigmiller160.authserver.dto.ExceptionConverter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(Exception::class)
  fun exception(ex: Exception): ResponseEntity<ErrorResponse> =
      ExceptionConverter.toErrorResponseEntity(ex)
}
