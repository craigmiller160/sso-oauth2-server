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

package io.craigmiller160.authserver.service

import io.craigmiller160.authserver.repository.ClientRepository
import io.craigmiller160.authserver.security.ClientUserDetails
import org.springframework.security.authentication.AccountStatusUserDetailsChecker
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class ClientUserDetailsService(
    private val clientRepo: ClientRepository
) : UserDetailsService {

    override fun loadUserByUsername(clientKey: String?): UserDetails {
        if (clientKey == null) {
            throw UsernameNotFoundException("No Client Key to lookup")
        }

        val client = clientRepo.findByClientKey(clientKey)
            ?: throw UsernameNotFoundException("No client found for Client Key $clientKey")
        val clientUserDetails = ClientUserDetails(client)
        AccountStatusUserDetailsChecker().check(clientUserDetails)
        return clientUserDetails
    }
}
