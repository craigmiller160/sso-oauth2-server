package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RoleRepository : JpaRepository<Role,Long> {

    @Query("""
        SELECT r
        FROM Role r
        WHERE r.id IN (
            SELECT cur.roleId
            FROM ClientUserRole cur
            WHERE cur.userId = :userId
            AND cur.clientId = :clientId
        )
    """)
    fun findAllByUserIdAndClientId(userId: Long, clientId: Long): List<Role>

}