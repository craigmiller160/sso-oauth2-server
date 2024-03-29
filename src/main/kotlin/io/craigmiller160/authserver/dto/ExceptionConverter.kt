package io.craigmiller160.authserver.dto

import java.time.ZonedDateTime
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object ExceptionConverter {
  private val log = LoggerFactory.getLogger(ExceptionConverter::class.java)

  fun toErrorResponseEntity(ex: Throwable): ResponseEntity<ErrorResponse> {
    log.error(ex.message, ex)
    val response = toErrorResponse(ex)
    return ResponseEntity.status(response.status).body(response)
  }
  fun toErrorResponse(ex: Throwable): ErrorResponse =
    when {
      isResponseStatusException(ex) -> handleResponseStatusException(ex)
      else -> defaultServerError(ex)
    }

  private fun isResponseStatusException(ex: Throwable): Boolean =
    ex.javaClass.getAnnotation(ResponseStatus::class.java) != null

  private fun defaultServerError(ex: Throwable): ErrorResponse {
    val (method, uri) = getMethodAndUri()
    return ErrorResponse(
      timestamp = ZonedDateTime.now(), status = 500, message = ex.message ?: "", method, uri)
  }

  private fun handleResponseStatusException(ex: Throwable): ErrorResponse {
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
          is ServletRequestAttributes -> it
          else -> null
        }
      }
      ?.request
      ?.let { request -> Pair(request.method, request.requestURI) }
      ?: Pair("", "")
}
