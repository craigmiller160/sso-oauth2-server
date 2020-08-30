package io.craigmiller160.authserver.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "client_redirect_uris")
data class ClientRedirectUri (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,
        val clientId: Long,
        val redirectUri: String
)
