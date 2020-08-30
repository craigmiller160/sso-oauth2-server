package io.craigmiller160.authserver.entity

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "clients")
data class Client (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,
        val name: String,
        val clientKey: String,
        val clientSecret: String,
        val enabled: Boolean,
        val accessTokenTimeoutSecs: Int,
        val refreshTokenTimeoutSecs: Int,
        val authCodeTimeoutSecs: Int,

        @OneToMany(cascade = [CascadeType.ALL, CascadeType.REMOVE], fetch = FetchType.EAGER, orphanRemoval = true)
        @JoinColumn(name = "clientId", insertable = false, updatable = false)
        val clientRedirectUris: List<ClientRedirectUri>
) {

        fun getRedirectUris(): List<String> {
                return clientRedirectUris.map { it.redirectUri }
        }

        fun supportsAuthCode(otherRedirectUri: String): Boolean {
                TODO("Finish this")
//                return authCodeTimeoutSecs != null &&
//                        redirectUri != null && redirectUri == otherRedirectUri
        }
}
