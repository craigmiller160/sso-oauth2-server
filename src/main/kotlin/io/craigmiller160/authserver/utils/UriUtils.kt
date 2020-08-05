package io.craigmiller160.authserver.utils

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

fun encodeUriParams(params: Map<String,String>): String {
    return params.map { entry ->
        val encodedKey = URLEncoder.encode(entry.key, StandardCharsets.UTF_8)
        val encodedValue = URLEncoder.encode(entry.value, StandardCharsets.UTF_8)
        "$encodedKey=$encodedValue"
    }
            .joinToString("&")
}
