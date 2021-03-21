package io.craigmiller160.authserver

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

fun main() {
    val secret = UUID.randomUUID().toString()
    println("SECRET: $secret")

    val encoder = BCryptPasswordEncoder()
    println("ENCODED: {bcrypt}${encoder.encode(secret)}")
}