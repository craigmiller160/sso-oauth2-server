package io.craigmiller160.ssoauthserverexp.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "Invalid login credentials")
class InvalidLoginException(msg: String) : RuntimeException(msg)