package io.craigmiller160.authserver.entity

import javax.persistence.*

@Entity
@Table(
        name = "roles",
        uniqueConstraints = [
                UniqueConstraint(columnNames = [
                        "name",
                        "clientId"
                ])
        ]
)
data class Role (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,
        val name: String,
        val clientId: Long
)
