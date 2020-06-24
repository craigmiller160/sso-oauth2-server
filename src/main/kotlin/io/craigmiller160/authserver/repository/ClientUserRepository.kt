package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.ClientUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientUserRepository : JpaRepository<ClientUser,Long>