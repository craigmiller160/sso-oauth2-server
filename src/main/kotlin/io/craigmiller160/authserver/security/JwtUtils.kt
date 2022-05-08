package io.craigmiller160.authserver.security

import io.craigmiller160.date.converter.LegacyDateConverter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

object JwtUtils {
  fun generateExp(expSecs: Int): Date {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    val exp = now.plusSeconds(expSecs.toLong())
    return LegacyDateConverter().convertZonedDateTimeToDate(exp)
  }

  fun generateNow(): Date {
    val now = ZonedDateTime.now(ZoneId.of("UTC"))
    return LegacyDateConverter().convertZonedDateTimeToDate(now)
  }
}
