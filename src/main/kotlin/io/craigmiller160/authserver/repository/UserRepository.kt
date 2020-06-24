package io.craigmiller160.authserver.repository

import io.craigmiller160.authserver.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User,Long> {

    @Query("""
        SELECT u 
        FROM User u 
        WHERE u.email = :email
        AND u.id IN (
            SELECT cu.userId
            FROM ClientUser cu
            WHERE cu.clientId = :clientId
        )
    """)
    fun findByEmailAndClientId(email: String, clientId: Long): User?

}