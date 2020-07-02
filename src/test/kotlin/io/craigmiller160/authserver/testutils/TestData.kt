package io.craigmiller160.authserver.testutils

import io.craigmiller160.authserver.dto.TokenRequest
import io.craigmiller160.authserver.entity.Client
import io.craigmiller160.authserver.entity.ClientUser
import io.craigmiller160.authserver.entity.ClientUserRole
import io.craigmiller160.authserver.entity.Role
import io.craigmiller160.authserver.entity.User
import io.craigmiller160.authserver.security.GrantType

object TestData {

    fun createTokenRequest(grantType: String, username: String? = null,
                           password: String? = null, refreshToken: String? = null,
                           clientId: String? = null, redirectUri: String? = null, code: String? = null): TokenRequest {
        return TokenRequest(grantType, username, password, refreshToken, clientId, code, redirectUri)
    }

    fun createClient(accessTokenTimeoutSecs: Int = 0, refreshTokenTimeoutSecs: Int = 0) = Client(
            id = 0,
            name = "Name",
            clientKey = "Key",
            clientSecret = "Secret",
            enabled = true,
            allowClientCredentials = true,
            allowAuthCode = true,
            allowPassword = true,
            accessTokenTimeoutSecs = accessTokenTimeoutSecs,
            refreshTokenTimeoutSecs = refreshTokenTimeoutSecs,
            authCodeTimeoutSecs = 0,
            redirectUri = null
    )

    fun createUser() = User(
            id = 0,
            email = "craig@gmail.com",
            firstName = "Craig",
            lastName = "Miller",
            password = "password"
    )

    fun createClientUser(userId: Long, clientId: Long) = ClientUser(
            id = 0,
            userId = userId,
            clientId = clientId
    )

    fun createRole1(clientId: Long) = Role(
            id = 0,
            name = "Role1",
            clientId = clientId
    )

    fun createRole2(clientId: Long) = Role(
            id = 0,
            name = "Role2",
            clientId = clientId
    )

    fun createClientUserRole(userId: Long, clientId: Long, roleId: Long) = ClientUserRole(
            id = 0,
            clientId = clientId,
            userId = userId,
            roleId = roleId
    )

}
