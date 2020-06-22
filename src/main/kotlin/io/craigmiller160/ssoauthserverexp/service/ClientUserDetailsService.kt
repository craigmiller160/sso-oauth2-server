package io.craigmiller160.ssoauthserverexp.service

import io.craigmiller160.ssoauthserverexp.repository.ClientRepository
import io.craigmiller160.ssoauthserverexp.security.ClientUserDetails
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
                ?: throw UsernameNotFoundException("No client found for client key $clientKey")
        val clientUserDetails = ClientUserDetails(client)
        AccountStatusUserDetailsChecker().check(clientUserDetails)
        return clientUserDetails
    }

}