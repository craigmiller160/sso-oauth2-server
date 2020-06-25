package io.craigmiller160.authserver.entity

import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "refresh_tokens")
data class RefreshToken (
        @Id
        val id: String,
        val refreshToken: String,
        val timestamp: LocalDateTime
)