package io.craigmiller160.authserver.entity

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(
        name = "refresh_tokens",
        uniqueConstraints = [
                UniqueConstraint(columnNames = [
                        "clientId",
                        "userId"
                ])
        ]
)
data class RefreshToken (
        @Id
        val id: String,
        @Lob
        val refreshToken: String,
        val clientId: Long,
        val userId: Long?,
        val timestamp: LocalDateTime
)
