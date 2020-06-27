package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.repository.ClientRepository
import org.springframework.data.domain.PageRequest

class UIService (
        private val clientRepo: ClientRepository
) {

    fun validateRequest(pageRequest: PageRequest) {
        TODO("Finish this")
    }

}
