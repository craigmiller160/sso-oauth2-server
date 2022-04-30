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

import io.craigmiller160.authserver.dto.PageRequest
import io.craigmiller160.authserver.exception.AuthCodeException
import io.craigmiller160.authserver.repository.ClientRepository
import org.springframework.stereotype.Service

@Service
class UIService(private val clientRepo: ClientRepository) {

  fun validateRequest(pageRequest: PageRequest) {
    if (pageRequest.response_type != "code") {
      throw AuthCodeException("Invalid response type")
    }

    if (pageRequest.redirect_uri.isEmpty()) {
      throw AuthCodeException("Invalid redirect_uri")
    }

    val client =
      clientRepo.findByClientKey(pageRequest.client_id)
        ?: throw AuthCodeException("Client not supported")

    if (!client.supportsAuthCode(pageRequest.redirect_uri)) {
      throw AuthCodeException("Client does not support Auth Code")
    }
  }
}
