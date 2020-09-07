package io.craigmiller160.authserver.entity

import javax.persistence.*

@Entity
@Table(name = "users")
data class User (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,
        @Column(unique = true)
        val email: String,
        val firstName: String,
        val lastName: String,
        val password: String,
        val enabled: Boolean
)
