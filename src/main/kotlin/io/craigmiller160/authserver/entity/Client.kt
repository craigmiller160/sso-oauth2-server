package io.craigmiller160.authserver.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
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
        val allowClientCredentials: Boolean,
        val allowPassword: Boolean,
        val allowAuthCode: Boolean,
        val accessTokenTimeoutSecs: Int,
        val refreshTokenTimeoutSecs: Int,
        val authCodeTimeoutSecs: Int?,
        val redirectUri: String?
)
