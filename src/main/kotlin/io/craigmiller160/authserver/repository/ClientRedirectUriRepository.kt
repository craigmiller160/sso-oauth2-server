package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.ClientRedirectUri
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientRedirectUriRepository : JpaRepository<ClientRedirectUri,Long>
