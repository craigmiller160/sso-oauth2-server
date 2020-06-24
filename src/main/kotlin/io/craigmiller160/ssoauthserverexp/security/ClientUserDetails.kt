package io.craigmiller160.ssoauthserverexp.security

import io.craigmiller160.ssoauthserverexp.entity.Client
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class ClientUserDetails(val client: Client): UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>()
        if (client.allowClientCredentials) {
            authorities += SimpleGrantedAuthority(ClientAuthorities.CLIENT_CREDENTIALS)
        }

        if(client.allowPassword) {
            authorities += SimpleGrantedAuthority(ClientAuthorities.PASSWORD)
        }

        if (client.allowAuthCode) {
            authorities += SimpleGrantedAuthority(ClientAuthorities.AUTH_CODE)
        }

        return authorities
    }

    override fun isEnabled(): Boolean {
        return client.enabled
    }

    override fun getUsername(): String {
        return client.clientKey
    }

    override fun isCredentialsNonExpired() = true

    override fun getPassword(): String {
        return client.clientSecret
    }

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

}

