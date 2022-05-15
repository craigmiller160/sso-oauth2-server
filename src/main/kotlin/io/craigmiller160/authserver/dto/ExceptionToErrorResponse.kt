package io.craigmiller160.authserver.dto

import java.time.ZonedDateTime
import org.springframework.web.bind.annotation.ResponseStatus

object ExceptionToErrorResponse {
  fun convert(ex: Exception): ErrorResponse {

    TODO()
  }

  private fun isResponseStatusException(ex: Throwable?): Boolean =
    ex != null && ex.javaClass.getAnnotation(ResponseStatus::class.java) != null

  private fun defaultServerError(ex: Throwable): ErrorResponse =
    ErrorResponse(timestamp = ZonedDateTime.now(), status = 500, message = ex.message ?: "", "", "")
}
