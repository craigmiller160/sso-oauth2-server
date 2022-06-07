package io.craigmiller160.authserver.security

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object CookieCreator {
  private const val DEFAULT_MAX_AGE = (24 * 60 * 60).toLong()
  private val COOKIE_EXP_FORMAT: DateTimeFormatter =
      DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z")

  fun create(cookieName: String, value: String, build: CookieBuilder.() -> Unit = {}): String {
    val expires = ZonedDateTime.now(ZoneId.of("GMT")).plusHours(24)
    val expiresString = COOKIE_EXP_FORMAT.format(expires)
    val cookieBuilder = CookieBuilder()
    cookieBuilder.build()
    val cookiePath = getCookiePath(cookieBuilder.path)

    return "$cookieName=$value; Max-Age=${cookieBuilder.maxAgeSecs}; Expires=$expiresString; Secure; HttpOnly; SameSite=strict; $cookiePath"
  }

  private fun getCookiePath(path: String?): String = path?.let { "Path=$it" } ?: ""

  class CookieBuilder {
    var maxAgeSecs: Long = DEFAULT_MAX_AGE
    var path: String? = null
  }
}
