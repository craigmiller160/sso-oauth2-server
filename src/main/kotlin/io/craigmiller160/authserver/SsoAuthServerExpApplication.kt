package io.craigmiller160.authserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SsoAuthServerExpApplication

fun main(args: Array<String>) {
	runApplication<SsoAuthServerExpApplication>(*args)
}
