package io.craigmiller160.authserver.security

import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

object JwtUtils {
  fun generateExp(expSecs: Int): Date {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val exp = now.plusSeconds(expSecs.toLong())
    return Date.from(exp.toInstant())
  }

  fun generateNow(): Date {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    return Date.from(now.toInstant())
  }
}
