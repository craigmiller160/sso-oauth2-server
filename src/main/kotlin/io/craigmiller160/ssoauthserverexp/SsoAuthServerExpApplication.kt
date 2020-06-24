package io.craigmiller160.ssoauthserverexp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@SpringBootApplication
class SsoAuthServerExpApplication

fun main(args: Array<String>) {
	println(BCryptPasswordEncoder().encode("password")) // TODO delete this

	runApplication<SsoAuthServerExpApplication>(*args)
}
