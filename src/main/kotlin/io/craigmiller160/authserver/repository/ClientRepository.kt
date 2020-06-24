package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientRepository : JpaRepository<Client,Long> {

    fun findByClientKey(clientKey: String): Client?

}