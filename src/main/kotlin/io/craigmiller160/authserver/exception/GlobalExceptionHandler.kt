package io.craigmiller160.authserver.exception

import com.fasterxml.jackson.databind.JsonMappingException
import java.time.ZonedDateTime
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler {
  @ExceptionHandler(HttpMessageNotWritableException::class)
  fun messageNotWriteableException(
    ex: HttpMessageNotWritableException
  ): ResponseEntity<ControllerError> =
    when (ex.cause) {
      null -> defaultServerError(ex)
      is JsonMappingException ->
        when {
          isResponseStatusException(ex.cause!!.cause) ->
            handleResponseStatusException(ex.cause!!.cause!!)
          else -> defaultServerError(ex)
        }
      else -> defaultServerError(ex)
    }

  @ExceptionHandler(Exception::class)
  fun exception(ex: Exception): ResponseEntity<ControllerError> =
    when {
      isResponseStatusException(ex) -> handleResponseStatusException(ex)
      else -> defaultServerError(ex)
    }

  private fun handleResponseStatusException(ex: Throwable): ResponseEntity<ControllerError> {
    val statusAnnotation = ex.javaClass.getAnnotation(ResponseStatus::class.java)
    return ResponseEntity.status(statusAnnotation.code.value())
      .body(
        ControllerError(
          timestamp = ZonedDateTime.now(),
          status = statusAnnotation.code.value(),
          message = ex.message ?: ""))
  }

  private fun isResponseStatusException(ex: Throwable?): Boolean =
    ex != null && ex.javaClass.getAnnotation(ResponseStatus::class.java) != null

  private fun defaultServerError(ex: Throwable): ResponseEntity<ControllerError> =
    ResponseEntity.status(500)
      .body(
        ControllerError(timestamp = ZonedDateTime.now(), status = 500, message = ex.message ?: ""))

  // TODO add path
  data class ControllerError(val timestamp: ZonedDateTime, val status: Int, val message: String)
}
