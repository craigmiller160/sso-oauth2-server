/*
 *     SSO OAuth2 Auth Server
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

package io.craigmiller160.authserver.security

object GrantType {
    const val CLIENT_CREDENTIALS = "client_credentials"
    const val PASSWORD = "password"
    const val REFRESH_TOKEN = "refresh_token"
    const val AUTH_CODE = "authorization_code"

    fun isGrantTypeSupported(grantType: String): Boolean {
        return PASSWORD == grantType || REFRESH_TOKEN == grantType || AUTH_CODE == grantType;
    }

}
