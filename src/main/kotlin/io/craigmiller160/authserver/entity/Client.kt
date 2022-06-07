/*
 *     sso-oauth2-server
 *     Copyright (C) 2020 Craig Miller
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.craigmiller160.authserver.entity

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "clients")
data class Client(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long,
    val name: String,
    val clientKey: String,
    val clientSecret: String,
    val enabled: Boolean,
    val accessTokenTimeoutSecs: Int,
    val refreshTokenTimeoutSecs: Int,
    val authCodeTimeoutSecs: Int,
    @OneToMany(
        cascade = [CascadeType.ALL],
        fetch = FetchType.EAGER,
        orphanRemoval = true,
        mappedBy = "clientId")
    val clientRedirectUris: List<ClientRedirectUri>
) {

  fun getRedirectUris(): List<String> {
    return clientRedirectUris.map { it.redirectUri }
  }

  fun supportsAuthCode(otherRedirectUri: String): Boolean {
    return getRedirectUris().contains(otherRedirectUri)
  }
}
