package io.craigmiller160.authserver.security

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object CookieCreator {
  private const val DEFAULT_MAX_AGE = (24 * 60 * 60).toLong()
  private val COOKIE_EXP_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z")

  fun create(cookieName: String, value: String, maxAgeSecs: Long = DEFAULT_MAX_AGE): String {
    val expires = ZonedDateTime.now(ZoneId.of("GMT")).plusHours(24)
    val expiresString = COOKIE_EXP_FORMAT.format(expires)

    return "$cookieName=$value; Max-Age=$maxAgeSecs; Expires=$expiresString; Secure; HttpOnly; SameSite=strict;"
  }
}
