package io.craigmiller160.ssoauthserverexp.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Unsupported OAuth2 Grant Type")
class UnsupportedGrantTypeException(msg: String) : RuntimeException(msg)