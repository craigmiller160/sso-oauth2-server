package io.craigmiller160.authserver.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "Invalid Refresh Token")
class InvalidRefreshTokenException (msg: String) : RuntimeException(msg)