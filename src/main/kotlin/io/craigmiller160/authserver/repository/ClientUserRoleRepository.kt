package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.ClientUserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientUserRoleRepository : JpaRepository<ClientUserRole,Long>