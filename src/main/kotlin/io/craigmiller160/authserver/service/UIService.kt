package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.dto.PageRequest
import io.craigmiller160.authserver.exception.BadRequestException
import io.craigmiller160.authserver.repository.ClientRepository
import org.springframework.stereotype.Service

@Service
class UIService (
        private val clientRepo: ClientRepository
) {

    fun validateRequest(pageRequest: PageRequest) {
        val client = clientRepo.findByClientKey(pageRequest.client_id) ?: throw BadRequestException("Client not supported")
        if (client.redirectUri == null || client.redirectUri != pageRequest.redirect_uri) {
            throw BadRequestException("Invalid redirect URI")
        }
    }

}
