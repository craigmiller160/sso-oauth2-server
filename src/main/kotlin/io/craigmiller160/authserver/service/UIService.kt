package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.dto.PageRequest
import io.craigmiller160.authserver.exception.AuthCodeException
import io.craigmiller160.authserver.repository.ClientRepository
import org.springframework.stereotype.Service

@Service
class UIService (
        private val clientRepo: ClientRepository
) {

    fun validateRequest(pageRequest: PageRequest) {
        if (pageRequest.response_type != "code") {
            throw AuthCodeException("Invalid response type")
        }

        if (pageRequest.redirect_uri.isEmpty()) {
            throw AuthCodeException("Invalid redirect_uri")
        }

        val client = clientRepo.findByClientKey(pageRequest.client_id)
                ?: throw AuthCodeException("Client not supported")

        if (!client.supportsAuthCode(pageRequest.redirect_uri)) {
            throw AuthCodeException("Client does not support Auth Code")
        }
    }

}
