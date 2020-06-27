package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.dto.PageRequest
import io.craigmiller160.authserver.repository.ClientRepository
import org.springframework.stereotype.Service

@Service
class UIService (
        private val clientRepo: ClientRepository
) {

    fun validateRequest(pageRequest: PageRequest) {
        TODO("Finish this")
    }

}
