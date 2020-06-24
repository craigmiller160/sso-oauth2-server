package io.craigmiller160.authserver.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Bad Request")
class BadRequestException(msg: String) : RuntimeException(msg)