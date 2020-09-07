package io.craigmiller160.authserver.entity

import javax.persistence.*

@Entity
@Table(
        name = "client_users",
        uniqueConstraints = [
                UniqueConstraint(columnNames = [
                        "userId",
                        "clientId"
                ])
        ]
)
data class ClientUser (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,
        val userId: Long,
        val clientId: Long
)
