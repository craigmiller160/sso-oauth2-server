package io.craigmiller160.authserver.entity

import javax.persistence.*

@Entity
@Table(
        name = "client_redirect_uris",
        uniqueConstraints = [
                UniqueConstraint(columnNames = [
                        "clientId",
                        "redirectUri"
                ])
        ]
)
data class ClientRedirectUri (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,
        val clientId: Long,
        val redirectUri: String
)
