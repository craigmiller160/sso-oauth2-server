package io.craigmiller160.authserver.dto

import java.time.ZonedDateTime
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object ExceptionToErrorResponse {
  fun convert(ex: Exception): ErrorResponse =
    when {
      isResponseStatusException(ex) -> handleResponseStatusException(ex)
      else -> defaultServerError(ex)
    }

  private fun isResponseStatusException(ex: Exception): Boolean =
    ex.javaClass.getAnnotation(ResponseStatus::class.java) != null

  private fun defaultServerError(ex: Exception): ErrorResponse {
    val (method, uri) = getMethodAndUri()
    return ErrorResponse(
      timestamp = ZonedDateTime.now(), status = 500, message = ex.message ?: "", method, uri)
  }

  private fun handleResponseStatusException(ex: Exception): ErrorResponse {
    val (method, uri) = getMethodAndUri()
    val statusAnnotation = ex.javaClass.getAnnotation(ResponseStatus::class.java)
    return ErrorResponse(
      timestamp = ZonedDateTime.now(),
      status = statusAnnotation.code.value(),
      message = ex.message ?: "",
      method,
      uri)
  }

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
