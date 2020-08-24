package io.craigmiller160.authserver.security

import io.craigmiller160.authserver.entity.Client
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class ClientUserDetails(val client: Client): UserDetails {

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf()
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

