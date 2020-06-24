package io.craigmiller160.ssoauthserverexp.repository

import io.craigmiller160.ssoauthserverexp.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User,Long> {

    fun findByEmail(email: String): User?

}