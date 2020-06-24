package io.craigmiller160.ssoauthserverexp.repository

import io.craigmiller160.ssoauthserverexp.entity.ClientUserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientUserRoleRepository : JpaRepository<ClientUserRole,Long>