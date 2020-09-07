package io.craigmiller160.authserver.entity

import javax.persistence.*

@Entity
@Table(
        name = "client_user_roles",
        uniqueConstraints = [
                UniqueConstraint(columnNames = [
                        "clientId",
                        "userId",
                        "roleId"
                ])
        ]
)
data class ClientUserRole (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,
        val clientId: Long,
        val userId: Long,
        val roleId: Long
)
