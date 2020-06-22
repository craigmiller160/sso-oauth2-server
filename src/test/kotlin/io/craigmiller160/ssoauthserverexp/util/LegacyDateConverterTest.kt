package io.craigmiller160.ssoauthserverexp.util

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import org.junit.jupiter.api.Test
import java.lang.RuntimeException

class LegacyDateConverterTest {

    private val legacyDateConverter = LegacyDateConverter()

    @Test
    fun test_convertLocalDateTimeToDate() {
        val localDateTime = LocalDateTime.of(2019, 1, 1, 1, 1)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val date = legacyDateConverter.convertLocalDateTimeToDate(localDateTime)
//        assertEquals("2019-01-01 01:01", format.format(date))
        throw RuntimeException()
    }

    @Test
    fun test_convertDateToLocalDateTime() {
        val date = Date(119, 0, 1, 1, 1)
        val localDateTime = legacyDateConverter.convertDateToLocalDateTime(date)
        val expected = LocalDateTime.of(2019, 1, 1, 1, 1)
//        assertEquals(expected, localDateTime)
        throw RuntimeException()
    }

}