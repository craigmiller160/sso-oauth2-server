package io.craigmiller160.authserver.dto

import java.time.ZonedDateTime
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object ExceptionToErrorResponse {
  fun convert(ex: Exception): ErrorResponse {

    TODO()
  }

  private fun isResponseStatusException(ex: Throwable?): Boolean =
    ex != null && ex.javaClass.getAnnotation(ResponseStatus::class.java) != null

  private fun defaultServerError(ex: Throwable): ErrorResponse =
    ErrorResponse(timestamp = ZonedDateTime.now(), status = 500, message = ex.message ?: "", "", "")

  private fun getMethodAndUri(): Pair<String, String> =
    RequestContextHolder.getRequestAttributes()
      ?.let {
        when (it) {
          is ServletRequestAttributes -> it as ServletRequestAttributes
          else -> null
        }
      }
      ?.request?.let { request -> Pair(request.method, request.pathInfo) }
      ?: Pair("", "")
}
