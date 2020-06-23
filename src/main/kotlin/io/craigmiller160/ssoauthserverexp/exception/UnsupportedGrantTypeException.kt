package io.craigmiller160.ssoauthserverexp.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ResponseStatusException
import java.lang.Exception

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Unsupported OAuth2 Grant Type")
class UnsupportedGrantTypeException(msg: String) : Exception(msg)