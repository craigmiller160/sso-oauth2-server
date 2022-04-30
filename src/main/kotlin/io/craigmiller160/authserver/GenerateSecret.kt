package io.craigmiller160.authserver

import java.util.*
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

fun main() {
  val secret = UUID.randomUUID().toString()
  println("SECRET: $secret")

  val encoder = BCryptPasswordEncoder()
  println("ENCODED: {bcrypt}${encoder.encode(secret)}")
}
